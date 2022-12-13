package ch.swisssign.ra.app.service.impl;

import static ch.swisssign.ra.gen.client.model.CertificateOrderStatusDto.FAILED;
import static ch.swisssign.ra.gen.client.model.CertificateOrderStatusDto.ISSUED;
import static org.springframework.http.HttpStatus.OK;

import ch.swisssign.ra.app.certificate.CertificateSigningRequestGenerator;
import ch.swisssign.ra.app.exception.RegistrationServiceException;
import ch.swisssign.ra.app.repository.IRegistrationAuthorityRepository;
import ch.swisssign.ra.app.service.IRegistrationService;
import ch.swisssign.ra.gen.client.model.CertificateOrderDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceImpl implements IRegistrationService {

  private final CertificateSigningRequestGenerator certificateSigningRequestGenerator;

  private final RetryTemplate retryTemplate;

  private final IRegistrationAuthorityRepository repository;

  public void validateDomains(final List<String> domains) throws RegistrationServiceException {
    final var client = repository.getClient();

    log.info("List of domains were requested to be prevalidated: {}", domains);

    final var prevalidatedDomainResponse = repository.getPrevalidatedDomains(client.getUuid());

    if (prevalidatedDomainResponse.getStatusCode() != OK) {
      throw new RegistrationServiceException(
          String.format(
              "Not expected API response during prevalidated domain listing: %s",
              prevalidatedDomainResponse.getStatusCode()));
    }

    final var prevalidatedDomains = Objects.requireNonNull(prevalidatedDomainResponse.getBody());

    final var findings =
        prevalidatedDomains.stream()
            .filter(x -> x.getStatus() != null && domains.contains(x.getDomain()))
            .toList();

    for (final var knownDomain : findings) {
      switch (Objects.requireNonNull(knownDomain.getStatus())) {
        case VALID -> {
          log.info("Domain {} is already validated", knownDomain.getDomain());
        }

        case PENDING -> {
          log.info(
              "Domain {} has a pending validation, please validate with TXT record: {}",
              knownDomain.getDomain(),
              Objects.requireNonNull(knownDomain.getRandomValue()));
        }

        case NOT_VALIDATED -> {
          log.info("Domain {} is not validated, trigger validation", knownDomain.getDomain());

          final var response =
              repository.prevalidatedDomain(client.getUuid(), knownDomain.getDomain());

          if (response.getStatusCode() != OK) {
            throw new RegistrationServiceException(
                String.format(
                    "Not expected API response during %s domain validation: %s",
                    knownDomain.getDomain(), response.getStatusCode()));
          }

          final var validationResponse =
              Objects.requireNonNull(response.getBody()).stream().findFirst();

          validationResponse.ifPresent(
              x ->
                  log.info(
                      "Set TXT record for {} (uuid={}): {}",
                      knownDomain.getDomain(),
                      x.getUuid(),
                      x.getRandomValue()));
        }

        case EXPIRED -> throw new RegistrationServiceException("Not implemented in this example");

        default -> log.warn(
            "Domain {} has unknown validation status, skipping", knownDomain.getDomain());
      }
    }
  }

  private void checkOrderStatus(final String orderUUID) {
    final var response = repository.getCertificateOrderStatus(orderUUID);

    if (response.getStatusCode() != OK) {
      throw new RegistrationServiceException(
          String.format(
              "Not expected API response during certificate order %s status check  domain validation: %s",
              orderUUID, response.getStatusCode()));
    }

    final var status = Objects.requireNonNull(response.getBody());

    if (status == FAILED) {
      throw new RuntimeException(String.format("%s certificate order failed", orderUUID));
    } else if (status != ISSUED) {
      log.info("{}: {}", orderUUID, status);

      throw new RegistrationServiceException(
          String.format("%s certificate order is not ready yet", orderUUID));
    }

    log.info("{} the certificate has been successfully issued", orderUUID);
  }

  @Override
  public void issueCertificate(final String productName, final List<String> domains)
      throws RegistrationServiceException {
    final var client = repository.getClient();

    final var product =
        Optional.ofNullable(client.getProducts()).orElseGet(Collections::emptyList).stream()
            .filter(
                productDto ->
                    StringUtils.hasText(productDto.getProductName())
                        && productDto.getProductName().equals(productName))
            .findAny()
            .orElseThrow(
                () ->
                    new RegistrationServiceException(
                        String.format("The requested product (%s) not found", productName)));

    String csr;

    try {
      csr = certificateSigningRequestGenerator.newRequest(domains);

    } catch (final IOException
        | NoSuchAlgorithmException
        | NoSuchProviderException
        | OperatorCreationException e) {
      throw new RegistrationServiceException("Could not generate Certificate Signing Request", e);
    }

    ResponseEntity<CertificateOrderDto> issueResponse;

    try {
      issueResponse = repository.issueCertificate(product.getUuid(), csr);
    } catch (final JsonProcessingException e) {
      throw new RuntimeException("Could not marshal Certificate Signing Request to JSON", e);
    }

    if (issueResponse.getStatusCode() != OK) {
      throw new RegistrationServiceException(
          String.format(
              "Not expected API response during certificate issuance for %s: %s",
              domains, issueResponse.getStatusCode()));
    }

    final var order = Objects.requireNonNull(issueResponse.getBody());

    log.info("Certificate order has been created: {}", order.getUuid());

    retryTemplate.execute(
        context -> {
          log.debug("Try to get certificate order {} status", order.getUuid());

          checkOrderStatus(order.getUuid());

          return null;
        });

    ResponseEntity<List<String>> chainResponse;

    chainResponse = repository.getCertificateChain(order.getUuid());

    final var chain = Objects.requireNonNull(chainResponse.getBody());

    chain.forEach(s -> log.info("{}", s));

    for (final String certi : chain) {
      log.info("{}", certi);
      final var k = Certificate.getInstance(Base64Utils.decodeFromString(certi));
      System.out.println(k.getSerialNumber());
    }
  }
}

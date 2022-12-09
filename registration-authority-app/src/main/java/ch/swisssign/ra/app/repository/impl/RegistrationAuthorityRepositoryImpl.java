package ch.swisssign.ra.app.repository.impl;

import ch.swisssign.ra.app.config.ApiConfiguration;
import ch.swisssign.ra.app.exception.RegistrationServiceException;
import ch.swisssign.ra.app.jws.JWSGenerator;
import ch.swisssign.ra.app.jws.SignedJWT;
import ch.swisssign.ra.app.repository.IRegistrationAuthorityRepository;
import ch.swisssign.ra.gen.client.api.ApiRegistrationApi;
import ch.swisssign.ra.gen.client.model.CertificateOrderDto;
import ch.swisssign.ra.gen.client.model.CertificateOrderStatusDto;
import ch.swisssign.ra.gen.client.model.ClientDNSDto;
import ch.swisssign.ra.gen.client.model.ClientDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;

@Repository
@RequiredArgsConstructor
@Slf4j
public final class RegistrationAuthorityRepositoryImpl implements IRegistrationAuthorityRepository {

  private final ApiRegistrationApi api;

  private final ApiConfiguration configuration;

  private final JWSGenerator jwsGenerator;

  private final ObjectMapper objectMapper;

  private Instant tokenExpireAt;

  private static <T> Collector<T, ?, T> getSingletonElement() {
    return Collectors.collectingAndThen(
        Collectors.toList(),
        list -> {
          if (list.size() != 1) {
            throw new RegistrationServiceException(
                String.format(
                    "Only one element is expected but there are %d elements", list.size()));
          }

          return list.get(0);
        });
  }

  private synchronized void refreshToken() {
    final var now = Instant.now();

    if (tokenExpireAt == null || now.isAfter(tokenExpireAt)) {
      final SignedJWT res = jwsGenerator.generateJWS();

      this.tokenExpireAt = res.expireAt();

      api.getApiClient().setBearerToken(res.token());

      log.debug("JWS has been configured for the client with expiration date: {}", res.expireAt());
    }

    log.debug("JWS is still valid until: {}", tokenExpireAt);
  }

  @Override
  public ResponseEntity<List<String>> getCertificateChain(final String orderUUID)
      throws RestClientException {
    refreshToken();

    return api.getCertificateChainWithHttpInfo(orderUUID);
  }

  @Override
  public ResponseEntity<CertificateOrderStatusDto> getCertificateOrderStatus(final String orderUUID)
      throws RestClientException {
    refreshToken();

    return api.getCertificateOrderStatusWithHttpInfo(orderUUID);
  }

  @Override
  public ClientDto getClient() throws RestClientException {
    refreshToken();

    final var clients = api.searchClients(configuration.client());

    return clients.stream().collect(getSingletonElement());
  }

  @Override
  public List<ClientDNSDto> getPrevalidatedDomains(final String clientUUID)
      throws RestClientException {
    refreshToken();

    return api.getClientPrevalidatedDomains(clientUUID);
  }

  @Override
  public ResponseEntity<CertificateOrderDto> issueCertificate(final String uuid, final String pem)
      throws JsonProcessingException, RestClientException {
    refreshToken();

    return api.issueCertificateWithHttpInfo(uuid, objectMapper.writeValueAsString(pem));
  }

  @Override
  public ResponseEntity<List<ClientDNSDto>> prevalidatedDomain(
      final String clientUUID, final String domain) throws RestClientException {
    refreshToken();

    return api.createClientPrevalidatedDomainsWithHttpInfo(
        clientUUID, Collections.singletonList(domain));
  }
}

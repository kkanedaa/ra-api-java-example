package ch.swisssign.ra.app.repository;

import ch.swisssign.ra.gen.client.model.CertificateOrderDto;
import ch.swisssign.ra.gen.client.model.CertificateOrderStatusDto;
import ch.swisssign.ra.gen.client.model.ClientDNSDto;
import ch.swisssign.ra.gen.client.model.ClientDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface IRegistrationAuthorityRepository {

  ResponseEntity<CertificateOrderStatusDto> getCertificateOrderStatus(final String orderUUID);

  ClientDto getClient();

  ResponseEntity<CertificateOrderDto> issueCertificate(final String uuid, final String pem)
      throws JsonProcessingException;

  ResponseEntity<List<String>> getCertificateChain(final String orderUUID);

  ResponseEntity<List<ClientDNSDto>> prevalidatedDomain(
      final String clientUUID, final String domain);

  List<ClientDNSDto> getPrevalidatedDomains(final String clientUUID);
}

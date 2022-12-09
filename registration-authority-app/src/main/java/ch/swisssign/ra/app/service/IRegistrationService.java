package ch.swisssign.ra.app.service;

import ch.swisssign.ra.app.exception.RegistrationServiceException;
import java.util.List;

public interface IRegistrationService {

  void validateDomains(final List<String> domains) throws RegistrationServiceException;

  void issueCertificate(final String productName, final List<String> domains)
      throws RegistrationServiceException;
}

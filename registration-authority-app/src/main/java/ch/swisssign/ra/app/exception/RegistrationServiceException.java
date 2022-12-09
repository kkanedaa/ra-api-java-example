package ch.swisssign.ra.app.exception;

import org.springframework.web.client.RestClientException;

public final class RegistrationServiceException extends RestClientException {
  public RegistrationServiceException(final String message) {
    this(message, null);
  }

  public RegistrationServiceException(final Throwable cause) {
    this(cause != null ? cause.getMessage() : null, cause);
  }

  public RegistrationServiceException(final String message, final Throwable cause) {
    super(message);

    if (cause != null) {
      super.initCause(cause);
    }
  }
}

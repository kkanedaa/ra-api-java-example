package ch.swisssign.ra.app.config;

import ch.swisssign.ra.app.exception.RegistrationServiceException;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public RetryTemplate retryTemplate() {
    final var retryTemplate = new RetryTemplate();

    final var backOffPolicy = new ExponentialBackOffPolicy();
    backOffPolicy.setInitialInterval(1000L);

    retryTemplate.setBackOffPolicy(backOffPolicy);

    final var retryPolicy =
        new SimpleRetryPolicy(
            10, Collections.singletonMap(RegistrationServiceException.class, true));

    retryTemplate.setRetryPolicy(retryPolicy);

    return retryTemplate;
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}

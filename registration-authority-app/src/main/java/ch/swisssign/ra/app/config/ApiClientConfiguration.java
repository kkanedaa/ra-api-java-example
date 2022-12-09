package ch.swisssign.ra.app.config;

import ch.swisssign.ra.gen.client.api.ApiRegistrationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class ApiClientConfiguration {

  private final ApiConfiguration configuration;

  @Bean
  @Primary
  public ApiRegistrationApi registrationApi() {
    final var registrationApi = new ApiRegistrationApi();

    registrationApi.getApiClient().setBasePath(configuration.basePath());

    return registrationApi;
  }
}

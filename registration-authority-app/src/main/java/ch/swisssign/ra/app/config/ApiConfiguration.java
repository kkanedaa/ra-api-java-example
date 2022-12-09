package ch.swisssign.ra.app.config;

import java.time.Duration;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConstructorBinding
@ConfigurationProperties(prefix = "ra.app.api", ignoreUnknownFields = false)
@Validated
public record ApiConfiguration(
    @NotNull @NotBlank String client,
    @NotNull @NotBlank String username,
    @NotNull @NotBlank String secret,
    @NotNull @NotBlank String basePath,
    @NotNull @DurationMin(seconds = 5) Duration tokenLifetime) {}

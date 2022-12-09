package ch.swisssign.ra.app.task;

import ch.swisssign.ra.app.service.IRegistrationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("validatedomain")
@RequiredArgsConstructor
public final class ValidateDomain implements CommandLineRunner {

  private final IRegistrationService registrationService;

  @Override
  public void run(String... args) throws Exception {
    log.info("Started ValidateDomain");

    registrationService.validateDomains(List.of("test.alexszakaly.me"));
  }
}

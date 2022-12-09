package ch.swisssign.ra.app.task;

import ch.swisssign.ra.app.service.IRegistrationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("issuecertificate")
@RequiredArgsConstructor
public final class IssueCertificate implements CommandLineRunner {

  private final IRegistrationService registrationService;

  public static void main(String[] args) {
    SpringApplication.run(IssueCertificate.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    log.info("Started IssueCertificate");

    registrationService.issueCertificate(
        "SwissSign DV SSL Silver Multi-Domain",
        List.of("test.alexszakaly.me", "lb.alexszakaly.me"));
  }
}

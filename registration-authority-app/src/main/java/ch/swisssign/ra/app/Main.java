package ch.swisssign.ra.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication(scanBasePackages = {"ch.swisssign.ra.app", "ch.swisssign.ra.gen"})
@ConfigurationPropertiesScan
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }
}

package ch.swisssign.ra.app.jws;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;

import ch.swisssign.ra.app.config.ApiConfiguration;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public final class JWSGenerator {

  private static final String AUDIENCE = "REST API";
  private static final String ISSUER = "SwissPKI";

  private final ApiConfiguration configuration;

  public SignedJWT generateJWS() throws InvalidKeyException {
    final var key = Keys.hmacShaKeyFor(configuration.secret().getBytes(UTF_8));

    final var now = Instant.now();

    final var expirationTime = now.plusSeconds(configuration.tokenLifetime().toSeconds());

    final var jws =
        Jwts.builder()
            .setSubject(configuration.username())
            .setIssuer(ISSUER)
            .setAudience(AUDIENCE)
            .setExpiration(Date.from(expirationTime))
            .setNotBefore(Date.from(now.minusSeconds(SECONDS.toSeconds(10))))
            .setIssuedAt(Date.from(now))
            .signWith(key, HS256)
            .compact();

    return new SignedJWT(jws, expirationTime);
  }
}

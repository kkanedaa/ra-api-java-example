package ch.swisssign.ra.app.jws;

import java.time.Instant;

public record SignedJWT(String token, Instant expireAt) {}

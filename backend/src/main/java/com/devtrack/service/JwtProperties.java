package com.devtrack.service;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devtrack.security.jwt")
public record JwtProperties(String secret, Duration expiration) {
}

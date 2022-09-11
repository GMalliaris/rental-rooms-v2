package org.gmalliaris.rental.rooms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@ConfigurationProperties(prefix = "jwt")
@ConstructorBinding
@Validated
public class JwtConfigurationProperties {

    private static final int ACCESS_TOKEN_MIN_EXPIRATION_SECONDS = 120;
    private static final int REFRESH_TOKEN_MIN_EXPIRATION_MINUTES = 60;
    private static final int REFRESH_TOKEN_EXPIRATION_MIN_THRESHOLD_SECONDS = 120;

    @Min(120)
    @Max(300)
    private final Integer accessExpirationSeconds;

    @Min(15)
    @Max(60)
    private final Integer refreshExpirationMinutes;

    @Min(60)
    @Max(300)
    private final Integer refreshExpirationThresholdSeconds;

    public JwtConfigurationProperties(Integer accessExpirationSeconds,
                                      Integer refreshExpirationMinutes,
                                      Integer refreshExpirationThresholdSeconds) {
        if (accessExpirationSeconds == null) {
            accessExpirationSeconds = ACCESS_TOKEN_MIN_EXPIRATION_SECONDS;
        }
        if (refreshExpirationMinutes == null) {
            refreshExpirationMinutes = REFRESH_TOKEN_MIN_EXPIRATION_MINUTES;
        }
        if (refreshExpirationThresholdSeconds == null) {
            refreshExpirationThresholdSeconds = REFRESH_TOKEN_EXPIRATION_MIN_THRESHOLD_SECONDS;
        }

        this.accessExpirationSeconds = accessExpirationSeconds;
        this.refreshExpirationMinutes = refreshExpirationMinutes;
        this.refreshExpirationThresholdSeconds = refreshExpirationThresholdSeconds;
    }

    public Integer getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }

    public Integer getRefreshExpirationMinutes() {
        return refreshExpirationMinutes;
    }

    public Integer getRefreshExpirationThresholdSeconds() {
        return refreshExpirationThresholdSeconds;
    }
}

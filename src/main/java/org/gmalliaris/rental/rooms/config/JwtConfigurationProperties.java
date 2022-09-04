package org.gmalliaris.rental.rooms.config;

import org.springframework.beans.factory.annotation.Value;
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

    @Min(120)
    @Max(300)
    private final Integer accessExpirationSeconds;

    @Min(15)
    @Max(60)
    private final Integer refreshExpirationMinutes;

    public JwtConfigurationProperties(Integer accessExpirationSeconds, Integer refreshExpirationMinutes) {
        if (accessExpirationSeconds == null) {
            accessExpirationSeconds = ACCESS_TOKEN_MIN_EXPIRATION_SECONDS;
        }
        if (refreshExpirationMinutes == null) {
            refreshExpirationMinutes = REFRESH_TOKEN_MIN_EXPIRATION_MINUTES;
        }

        this.accessExpirationSeconds = accessExpirationSeconds;
        this.refreshExpirationMinutes = refreshExpirationMinutes;
    }

    public Integer getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }

    public Integer getRefreshExpirationMinutes() {
        return refreshExpirationMinutes;
    }
}

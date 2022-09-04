package org.gmalliaris.rental.rooms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "spring.redis")
@ConstructorBinding
public class RedisConfigurationProperties {

    @NotBlank
    private final String host;

    @NotNull
    private final Integer port;

    @NotBlank
    private final String password;

    public RedisConfigurationProperties(String host, Integer port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }
}

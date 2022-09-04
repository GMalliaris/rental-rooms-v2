package org.gmalliaris.rental.rooms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfiguration.class);
    private static final String CONNECTION_CHECK = "check";

    private final RedisConfigurationProperties redisConfigurationProperties;


    public RedisConfiguration(RedisConfigurationProperties redisConfigurationProperties) {
        this.redisConfigurationProperties = redisConfigurationProperties;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        final var redisTemplate = new RedisTemplate<String, String>();

        var serializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(serializer);
        redisTemplate.setValueSerializer(serializer);

        var configuration = new RedisStandaloneConfiguration(redisConfigurationProperties.getHost(),
                redisConfigurationProperties.getPort());
        configuration.setPassword(RedisPassword.of(redisConfigurationProperties.getPassword()));
        var jedisClientConfiguration = JedisClientConfiguration.builder().build();
        var jedisConnectionFactory = new JedisConnectionFactory(configuration, jedisClientConfiguration);
        jedisConnectionFactory.afterPropertiesSet();

        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();


        try {
            redisTemplate.opsForValue().get(CONNECTION_CHECK);
        }
        catch (RedisConnectionFailureException ex) {
            LOGGER.error("Failed to connect to redis cache");
            throw ex;
        }

        return redisTemplate;
    }
}

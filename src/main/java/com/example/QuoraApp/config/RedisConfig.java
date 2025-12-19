package com.example.QuoraApp.config;

import com.example.QuoraApp.dto.QuestionResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature; // Import this
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, QuestionResponseDTO> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {

        // 1. Create the ObjectMapper and register the JavaTimeModule
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                // Best practice: make dates human-readable strings, not timestamps
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 2. Create the serializer, passing in the configured ObjectMapper
        Jackson2JsonRedisSerializer<QuestionResponseDTO> valueSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, QuestionResponseDTO.class);

        // 3. Define the key serializer
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        // 4. Build the serialization context using your configured serializers
        RedisSerializationContext<String, QuestionResponseDTO> context = RedisSerializationContext
                .<String, QuestionResponseDTO>newSerializationContext(keySerializer)
                .value(valueSerializer)
                .hashKey(keySerializer)
                .hashValue(valueSerializer)
                .build();

        // 5. Create and return the template with the correct context
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
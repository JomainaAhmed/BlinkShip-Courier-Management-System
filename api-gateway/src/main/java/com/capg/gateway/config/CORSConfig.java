package com.capg.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS is declaratively configured via application.properties / Spring Cloud Gateway
 * globalcors configuration to prevent dual CORS definitions and invalid wildcard credentials.
 */
@Configuration
public class CORSConfig {
    // Programmatic CorsWebFilter removed in favor of standard declarative gateway globalcors configuration
}

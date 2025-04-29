package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.HttpHeaders;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.cloud.context.config.annotation.RefreshScope;


@Configuration
public class LogbookConfig {
    
    private final BodyFilterProperties bodyFilterProps;
    
    // Constructor injection to ensure we always get the refreshed instance
    public LogbookConfig(BodyFilterProperties bodyFilterProps) {
        this.bodyFilterProps = bodyFilterProps;
    }

    @Bean
    @RefreshScope
    public BodyFilter bodyFilter() {
        return (contentType, body) -> {
            String result = body;
            
            // Apply all configured regex patterns from the properties
            for (BodyFilterProperties.PatternReplacement pattern : bodyFilterProps.getPatterns()) {
                result = result.replaceAll(pattern.getPattern(), pattern.getReplacement());
            }
            
            return result;
        };
    }

    @Bean
    public HeaderFilter headerFilter() {
        return headers -> {
            final Map<String, List<String>> filtered = new HashMap<>(headers);
            
            // Case-insensitive header masking
            for (String key : headers.keySet()) {
                // Authorization headers (Bearer tokens, Basic auth)
                if (key.equalsIgnoreCase("Authorization")) {
                    List<String> values = headers.get(key);
                    List<String> maskedValues = values.stream()
                        .map(value -> {
                            if (value.startsWith("Bearer ")) {
                                return "Bearer ****";
                            } else if (value.startsWith("Basic ")) {
                                return "Basic ****";
                            }
                            return "****";
                        })
                        .collect(Collectors.toList());
                    filtered.put(key, maskedValues);
                }
                
                // API keys
                else if (key.equalsIgnoreCase("X-API-Key")) {
                    filtered.put(key, Collections.singletonList("****"));
                }
                
                // Security tokens
                else if (key.equalsIgnoreCase("X-Security-Token")) {
                    filtered.put(key, Collections.singletonList("****"));
                }
                
                // Session IDs
                else if (key.equalsIgnoreCase("X-Session-Id")) {
                    filtered.put(key, Collections.singletonList("****"));
                }
                
                // Cookies containing session data
                else if (key.equalsIgnoreCase("Cookie")) {
                    List<String> cookies = headers.get(key);
                    List<String> maskedCookies = cookies.stream()
                        .map(cookie -> cookie.replaceAll("(JSESSIONID|session|auth_token)=[^;]+", "$1=****"))
                        .collect(Collectors.toList());
                    filtered.put(key, maskedCookies);
                }
            }
            
            return HttpHeaders.of(filtered);
        };
    }
} 
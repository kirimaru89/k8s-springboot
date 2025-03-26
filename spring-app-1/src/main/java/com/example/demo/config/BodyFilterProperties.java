package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "logbook.body.filter")
@RefreshScope
public class BodyFilterProperties {
    private List<PatternReplacement> patterns = new ArrayList<>();

    // Default constructor with sensible defaults
    public BodyFilterProperties() {
        // Default patterns if ConfigMap isn't available
        patterns.add(new PatternReplacement(
            "\\b\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}\\b", 
            "****-****-****-$4"));
    }

    public List<PatternReplacement> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<PatternReplacement> patterns) {
        this.patterns = patterns;
    }

    public static class PatternReplacement {
        private String pattern;
        private String replacement;

        public PatternReplacement() { }

        public PatternReplacement(String pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }

        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }
        public String getReplacement() { return replacement; }
        public void setReplacement(String replacement) { this.replacement = replacement; }
    }
}
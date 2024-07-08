package com.svarto.repo_generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "github")
public class GitHubProperties {
    private final String username;
    private final String accessToken;
    private final String apiUrl;

    public GitHubProperties(String username, String accessToken, String apiUrl) {
        this.username = username;
        this.accessToken = accessToken;
        this.apiUrl = apiUrl;
    }
}

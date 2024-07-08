package com.svarto.repo_generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Data
@ConfigurationProperties(prefix = "bitbucket")
public class BitbucketProperties {
    private final String username;
    private final String password;
    private final String ownerName;
    private final String apiUrl;


    public BitbucketProperties(String username, String password, String ownerName, String apiUrl) {
        this.username = username;
        this.password = password;
        this.ownerName = ownerName;
        this.apiUrl = apiUrl;
    }
}

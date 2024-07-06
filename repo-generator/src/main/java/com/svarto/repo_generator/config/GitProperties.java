package com.svarto.repo_generator.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Data
@Component
public class GitProperties {
    @Value("${github.username}")
    private String githubUsername;

    @Value("${github.accessToken}")
    private String githubAccessToken;

    @Value("${github.api-url}")
    private String githubApiUrl;

    @Value("${bitbucket.username}")
    private String bitbucketUsername;

    @Value("${bitbucket.password}")
    private String bitbucketPassword;

    @Value("${bitbucket.owner-name}")
    private String bitbucketOwnerName;

    @Value("${bitbucket.api-url}")
    private String bitbucketApiUrl;


}

package com.svarto.repo_generator.service;

import com.svarto.repo_generator.config.GitProperties;
import com.svarto.repo_generator.model.GitProvider;
import org.springframework.stereotype.Component;

@Component
public class GitServiceFactory {
    private String githubUsername;
    private String githubAccessToken;
    private String githubApiUrl;
    private String bitbucketUsername;
    private String bitbucketPassword;
    private String bitbucketOwnerName;
    private String bitbucketApiUrl;
    private final GitProperties gitProperties;

    public GitServiceFactory(GitProperties gitProperties) {
        this.gitProperties = gitProperties;
        this.githubUsername = gitProperties.getGithubUsername();
        this.githubAccessToken = gitProperties.getGithubAccessToken();
        this.githubApiUrl = gitProperties.getGithubApiUrl();
        this.bitbucketUsername = gitProperties.getBitbucketUsername();
        this.bitbucketPassword = gitProperties.getBitbucketPassword();
        this.bitbucketOwnerName = gitProperties.getBitbucketOwnerName();
        this.bitbucketApiUrl = gitProperties.getBitbucketApiUrl();
    }

    public SourceService createSourceService(GitProvider provider) {
        switch (provider) {
            case GITHUB:
                return new GitHubSourceService(githubUsername, githubAccessToken, githubApiUrl);
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);

        }
    }

    public TargetService createTargetService(GitProvider provider) {
        switch (provider) {
            case BITBUCKET:
                return new BitbucketTargetService(bitbucketUsername, bitbucketPassword, bitbucketOwnerName, bitbucketApiUrl);
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }
}

package com.svarto.repo_generator.service;

import com.svarto.repo_generator.model.GitProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GitServiceFactory {
    private final BitbucketTargetService bitbucketTargetService;
    private final GitHubSourceService gitHubSourceService;
    @Autowired
    public GitServiceFactory(BitbucketTargetService bitbucketTargetService, GitHubSourceService gitHubSourceService) {
        this.bitbucketTargetService  = bitbucketTargetService;
        this.gitHubSourceService = gitHubSourceService;

    }

    public SourceService createSourceService(GitProvider provider) {
        switch (provider) {
            case GITHUB:
                return gitHubSourceService;
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);

        }
    }

    public TargetService createTargetService(GitProvider provider) {
        switch (provider) {
            case BITBUCKET:
                return bitbucketTargetService;
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }
}

package com.svarto.repo_generator.service;

import java.net.URISyntaxException;

public interface TargetService {
    public void uploadAllRepositoriesToBitbucket(String localRepositoriesDir) throws URISyntaxException;

    public void uploadRepositoryToBitbucket(String localRepositoriesDir, String localRepo) throws URISyntaxException;

    public void createBitbucketRepository(String repoName);
}

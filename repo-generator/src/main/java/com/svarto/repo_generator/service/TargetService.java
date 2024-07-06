package com.svarto.repo_generator.service;

import java.net.URISyntaxException;

public interface TargetService {
    public void uploadAllRepositories(String localRepositoriesDir) throws URISyntaxException;

    public void uploadRepository(String localRepositoriesDir, String localRepo) throws URISyntaxException;

    public void createRepository(String repoName);
}

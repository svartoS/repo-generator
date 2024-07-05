package com.svarto.repo_generator.service;

import com.svarto.repo_generator.model.Repository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class GithubRepositorySynchronizationService {
    @Autowired
    private GitHubService githubService;
    @Autowired
    private GitService gitService;

    @Value("${git.remote.path}")
    private String remotePath;

    public void syncAllLocalRepositories(String localPathREST) throws GitAPIException {
        List<Repository> repositories = githubService.getRepositories();
        for (Repository repository : repositories) {
            repository.setLocalPath(localPathREST + "/" + repository.getName());
            try {
                if (!new File(repository.getLocalPath()).exists()) {
                    gitService.cloneRepository(repository);
                } else {
                    gitService.updateRepository(repository);
                }
            } catch (Exception e) {
                log.error("Произошла ошибка при синхронизации репозиториев: " + e.getMessage());
                throw e;
            }
        }
    }

    public void syncLocalRepository(String localPathREST, String repoName) throws GitAPIException {
        try {
            Repository repository = githubService.getRepository(repoName);
            repository.setLocalPath(localPathREST + "/" + repoName);
            if (!new File(repository.getLocalPath()).exists()) {
                gitService.cloneRepository(repository);
            } else {
                gitService.updateRepository(repository);
            }
        } catch (HttpClientErrorException e) {
            log.error("HttpClientErrorException: " + e.getMessage());
            throw new HttpClientErrorException(e.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
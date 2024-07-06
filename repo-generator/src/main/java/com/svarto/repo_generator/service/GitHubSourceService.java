package com.svarto.repo_generator.service;

import com.svarto.repo_generator.model.Branch;
import com.svarto.repo_generator.model.GitProvider;
import com.svarto.repo_generator.model.Repository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
@Service
public class GitHubSourceService implements SourceService {
    private String username;
    private String accessToken;
    private String apiUrl;
    private RestTemplate restTemplate;
    private GitService gitService;

    public GitHubSourceService(String githubUsername, String githubAccessToken, String githubApiUrl) {
        this.username = githubUsername;
        this.accessToken = githubAccessToken;
        this.apiUrl = githubApiUrl;
        this.restTemplate = new RestTemplate();
        this.gitService = new GitService(githubUsername, githubAccessToken);
    }

    public List<Repository> getRepositories() {
        String url = apiUrl + "/user/repos";
        log.info("Получение репозиториев по URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Repository[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Repository[].class);
            log.info("Репозитории с Github были успешно получены");

            return Arrays.stream(response.getBody()).map(repo -> {
                List<Branch> branches = getBranches(repo.getUrl());
                repo.setBranches(branches);

                return repo;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Ошибка получения репозитория с Github: {}", e);
            return Collections.emptyList();
        }
    }

    public Repository getRepository(String repoName) {
        String url = apiUrl + "/repos/" + username + "/" + repoName;
        log.info("Получение репозиториев по URL: {}", url);

        HttpEntity<Void> entity = createHttpEntity();

        try {
            ResponseEntity<Repository> response = restTemplate.exchange(url, HttpMethod.GET, entity, Repository.class);
            log.info("Репозиторий с Github был успешно получен: {}", repoName);

            Repository repo = response.getBody();

            if (repo != null) {
                List<Branch> branches = getBranches(repo.getUrl());
                repo.setBranches(branches);
            }

            return repo;
        } catch (Exception e) {
            log.error("Ошибка получения репозитория с Github: {}", repoName, e);
            throw e;
        }

    }

    public void syncAllLocalRepositories(String localPathREST) {
        List<Repository> repositories = getRepositories();
        for (Repository repository : repositories) {
            repository.setLocalPath(localPathREST + "/" + repository.getName());
            try {
                if (!new File(repository.getLocalPath()).exists()) {
                    gitService.cloneRepository(repository);
                } else {
                    gitService.updateRepository(repository);
                }
            } catch (HttpClientErrorException e) {
                log.error("Произошла ошибка HTTP-запроса: {} ", e);
                throw new HttpClientErrorException(e.getStatusCode());
            } catch (GitAPIException e) {
                log.error("Произошла ошибка JGit: {} ", e);
                throw new JGitInternalException(e.getMessage());
            } catch (Exception e) {
                // Обработка других ошибок
                log.error("Произошла ошибка: {}", e);
                throw new RuntimeException(e);
            }
        }
    }

    public void syncLocalRepository(String localPathREST, String repoName) {
        try {
            Repository repository = getRepository(repoName);
            repository.setLocalPath(localPathREST + "/" + repoName);
            if (!new File(repository.getLocalPath()).exists()) {
                gitService.cloneRepository(repository);
            } else {
                gitService.updateRepository(repository);
            }
        } catch (HttpClientErrorException e) {
            log.error("Произошла ошибка HTTP-запроса: {} ", e);
            throw new HttpClientErrorException(e.getStatusCode());
        } catch (GitAPIException e) {
            log.error("Произошла ошибка JGit: {} ", e);
            throw new JGitInternalException(e.getMessage());
        } catch (Exception e) {
            log.error("Произошла ошибка: {}", e);
            throw new RuntimeException(e);
        }
    }

    public List<Branch> getBranches(String repoUrl) {
        String url = repoUrl + "/branches";

        HttpEntity<Void> entity = createHttpEntity();
        ResponseEntity<Branch[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Branch[].class);

        return Arrays.stream(response.getBody()).collect(Collectors.toList());
    }

    private HttpEntity<Void> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);

        return new HttpEntity<>(headers);
    }
}
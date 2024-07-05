package com.svarto.repo_generator.service;

import com.svarto.repo_generator.model.Branch;
import com.svarto.repo_generator.model.GitProvider;
import com.svarto.repo_generator.model.Repository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GitHubService implements SourceService {
    @Autowired
    private RestTemplate restTemplate;
    @Value("${github.username}")
    private String GITHUB_USERNAME;
    @Value("${github.accessToken}")
    private String GITHUB_ACCESS_TOKEN;
    @Value("${github.api-url}")
    private String GITHUB_API_URL;

    public List<Repository> getRepositories() {
        String url = GITHUB_API_URL + "/user/repos";
        System.out.println(url);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + GITHUB_ACCESS_TOKEN);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Repository[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Repository[].class);

        return Arrays.stream(response.getBody()).map(repo -> {
            repo.setProvider(GitProvider.GITHUB);
            List<Branch> branches = getBranches(repo.getUrl());
            repo.setBranches(branches);

            return repo;
        }).collect(Collectors.toList());
    }

    public Repository getRepository(String repoName) {
        String url = GITHUB_API_URL + "/repos/" +  GITHUB_USERNAME +"/"  + repoName;
        HttpEntity<Void> entity = createHttpEntity();
        ResponseEntity<Repository> response = restTemplate.exchange(url, HttpMethod.GET, entity, Repository.class);

        Repository repo = response.getBody();
        if (repo != null) {
            repo.setProvider(GitProvider.GITHUB);
            List<Branch> branches = getBranches(repo.getUrl());
            repo.setBranches(branches);
        }

        return repo;
    }

    public List<Branch> getBranches(String repoUrl) {
        String url = repoUrl + "/branches";
        HttpEntity<Void> entity = createHttpEntity();
        ResponseEntity<Branch[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, Branch[].class);

        return Arrays.stream(response.getBody()).collect(Collectors.toList());
    }
    private HttpEntity<Void> createHttpEntity(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + GITHUB_ACCESS_TOKEN);

        return new HttpEntity<>(headers);
    }
}
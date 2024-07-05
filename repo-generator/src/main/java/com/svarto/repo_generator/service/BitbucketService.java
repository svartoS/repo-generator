package com.svarto.repo_generator.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
@Service
public class BitbucketService  implements TargetService{
    @Autowired
    private RestTemplate restTemplate;

    @Value("${bitbucket.username}")
    private String BITBUCKET_USERNAME;

    @Value("${bitbucket.password}")
    private String BITBUCKET_PASSWORD;

    @Value("${bitbucket.owner-name}")
    private String BITBUCKET_OWNER_NAME;

    @Value("${bitbucket.api-url}")
    private String BITBUCKET_API_URL;


    public void uploadAllRepositoriesToBitbucket(String localRepositoriesDir) throws URISyntaxException {
        File[] repositoryDirs = new File(localRepositoriesDir).listFiles(File::isDirectory);
        if (repositoryDirs == null) {
            return;
        }

        for (File repositoryDir : repositoryDirs) {
            String repoName = repositoryDir.getName();

            if (!new File(repositoryDir, ".git").exists()) {
                continue;
            }

            if (!bitbucketRepositoryExists(repoName)) {
                createBitbucketRepository(repoName);
            }

            try {
                Git git = Git.open(repositoryDir);
                URIish remoteURI = new URIish("https://bitbucket.org/" + BITBUCKET_OWNER_NAME + "/"  + repoName);

                git.getRepository().getConfig().setString("remote", "bitbucket", "url", remoteURI.toString());
                git.getRepository().getConfig().save();

                // Отправляем все ветки на Bitbucket
                git.push()
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(BITBUCKET_USERNAME, BITBUCKET_PASSWORD))
                        .setRemote("bitbucket")
                        .setPushAll()
                        .setPushTags()
                        .call();

                git.close();
            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
            }
        }
    }

    public void uploadRepositoryToBitbucket(String localRepositoriesDir, String localRepo) throws URISyntaxException {
        File repositoryDir = new File(localRepositoriesDir, localRepo);

        if (!repositoryDir.exists() || !new File(repositoryDir, ".git").exists()) {
            System.out.println("Repository does not exist: " + localRepo);
            return;
        }

        if (!bitbucketRepositoryExists(localRepo)) {
            createBitbucketRepository(localRepo);
        }

        try {
            Git git = Git.open(repositoryDir);
            URIish remoteURI = new URIish("https://bitbucket.org/" + BITBUCKET_OWNER_NAME + "/" + repositoryDir.getName());

            git.getRepository().getConfig().setString("remote", "bitbucket", "url", remoteURI.toString());
            git.getRepository().getConfig().save();

            git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(BITBUCKET_USERNAME, BITBUCKET_PASSWORD))
                    .setRemote("bitbucket")
                    .setPushAll()
                    .setPushTags()
                    .call();

            git.close();
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
    }

    public void createBitbucketRepository(String repoName) {
        String url = BITBUCKET_API_URL + "/repositories/" + BITBUCKET_OWNER_NAME + "/" + repoName.toLowerCase();
        HttpHeaders headers = createHeaders(BITBUCKET_USERNAME, BITBUCKET_PASSWORD);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"scm\": \"git\", \"is_private\": true, \"fork_policy\": \"no_public_forks\"}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        System.out.println(url);
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    private boolean bitbucketRepositoryExists(String repoName) {
        String url = BITBUCKET_API_URL + "/repositories/" + BITBUCKET_OWNER_NAME + "/" + repoName.toLowerCase();
        HttpHeaders headers = createHeaders(BITBUCKET_USERNAME, BITBUCKET_PASSWORD);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private HttpHeaders createHeaders(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        return headers;
    }
}



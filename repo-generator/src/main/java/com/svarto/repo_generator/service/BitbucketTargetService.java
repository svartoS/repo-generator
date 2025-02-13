package com.svarto.repo_generator.service;

import com.svarto.repo_generator.config.BitbucketProperties;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.LockFailedException;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
@Service
public class BitbucketTargetService implements TargetService {
    private final String username;
    private final String password;
    private final String ownerName;
    private final String apiUrl;
    private final RestTemplate restTemplate;
    private final UsernamePasswordCredentialsProvider credentialsProvider;
    @Autowired
    public BitbucketTargetService(BitbucketProperties bitbucketProperties, UsernamePasswordCredentialsProvider bitbucketCredentialsProvider) {
        this.username = bitbucketProperties.getUsername();
        this.password = bitbucketProperties.getPassword();
        this.ownerName = bitbucketProperties.getOwnerName();
        this.apiUrl = bitbucketProperties.getApiUrl();
        this.restTemplate = new RestTemplate();
        this.credentialsProvider = bitbucketCredentialsProvider;
    }

    public void uploadAllRepositories(String localRepositoriesDir) throws URISyntaxException {
        File[] repositoryDirs = new File(localRepositoriesDir).listFiles(File::isDirectory);
        if (repositoryDirs == null) {
            log.warn("Директория не найдена в {}", localRepositoriesDir);
            return;
        }

        for (File repositoryDir : repositoryDirs) {
            String repoName = repositoryDir.getName();

            log.info("Обработка репозитория: {}", repoName);
            if (!new File(repositoryDir, ".git").exists()) {
                log.info("Пропуск non-git директории: {}", repoName);
                continue;
            }

            if (!bitbucketRepositoryExists(repoName)) {
                log.info("Репозиторий не был создан на Bitbucket. Создание репозитория: {}", repoName);
                createRepository(repoName);
            }

            try {
                Git git = Git.open(repositoryDir);
                URIish remoteURI = new URIish("https://bitbucket.org/" + ownerName + "/" + repoName);

                git.getRepository().getConfig().setString("remote", "bitbucket", "url", remoteURI.toString());
                git.getRepository().getConfig().save();

                git.push()
                        .setCredentialsProvider(credentialsProvider)
                        .setRemote("bitbucket")
                        .setPushAll()
                        .setPushTags()
                        .call();

                git.close();

                log.info("Репозиторий был успешно запушен: {}", repoName);
            } catch (IOException | GitAPIException e) {
                log.error("Ошибка пуша репозитория: {}", repoName, e);
            }
        }
    }

    public void uploadRepository(String localRepositoriesDir, String repoName) throws URISyntaxException {
        log.info("Обработка репозитория: {}", repoName);
        File repositoryDir = new File(localRepositoriesDir, repoName);

        if (!repositoryDir.exists() || !new File(repositoryDir, ".git").exists()) {
            log.warn("Директория не найдена в {}", repositoryDir);
            return;
        }

        if (!bitbucketRepositoryExists(repoName)) {
            log.info("Репозиторий не был создан на Bitbucket. Создание репозитория: {}", repoName);
            createRepository(repoName);
        }

        try {
            Git git = Git.open(repositoryDir);
            URIish remoteURI = new URIish("https://bitbucket.org/" + ownerName + "/" + repositoryDir.getName());

            git.getRepository().getConfig().setString("remote", "bitbucket", "url", remoteURI.toString());
            git.getRepository().getConfig().save();

            git.push()
                    .setCredentialsProvider(credentialsProvider)
                    .setRemote("bitbucket")
                    .setPushAll()
                    .setPushTags()
                    .call();

            git.close();

            log.info("Репозиторий был успешно запушен: {}", repoName);
        }
        catch (LockFailedException e) {
            log.error("Не удалось заблокировать файл конфигурации. Убедитесь, что ни один другой процесс не использует репозиторий: {}", repoName, e);
        }
        catch (IOException | GitAPIException e) {
            log.error("Ошибка пуша репозитория: {}", repoName, e);
        }
    }

    public void createRepository(String repoName) {
        try {
            String url = apiUrl + "/repositories/" + ownerName + "/" + repoName.toLowerCase();
            HttpHeaders headers = createHeaders(username, password);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = "{\"scm\": \"git\", \"is_private\": true, \"fork_policy\": \"no_public_forks\"}";
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            log.error("Ошибка", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean bitbucketRepositoryExists(String repoName) {
        String url = apiUrl + "/repositories/" + ownerName + "/" + repoName.toLowerCase();
        HttpHeaders headers = createHeaders(username, password);
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



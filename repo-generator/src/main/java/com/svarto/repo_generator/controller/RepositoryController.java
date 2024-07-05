package com.svarto.repo_generator.controller;


import com.svarto.repo_generator.model.Repository;
import com.svarto.repo_generator.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/")
@Tag(name = "RepositoryController", description = "Именно он за всё отвечает!")
public class RepositoryController {
    @Autowired
    private GitHubService githubService;

    @Autowired
    private BitbucketService bitbucketService;

    @Autowired
    private GithubRepositorySynchronizationService githubRepositorySynchronizationService;

    @GetMapping("/repositories")
    @Operation(summary = "Получение списка всех репозиториев с Github",
            description = "Возвращает список репозиториев",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved repositories"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public ResponseEntity<List<Repository>> getRepositories() {
        try {
            List<Repository> repositories = githubService.getRepositories();
            return new ResponseEntity<>(repositories, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/sync/local/all")
    @Operation(
            summary = "Обновление репозиториев",
            description = "Позволяет склонировать или обновить ВСЕ репозитории с Github на диск"
    )
    public ResponseEntity<Void> syncAllLocalRepositories(@RequestParam("localPath") String localPath) {
        try {
            githubRepositorySynchronizationService.syncAllLocalRepositories(localPath);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/sync/local/")
    @Operation(summary = "Обновление конкретного репозитория с Github",
            description = "Клонирует или обновляет конкретный репозиторий",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Репозиторий  был успешно обработан"),
                    @ApiResponse(responseCode = "404", description = "Не удалось найти данный репозиторий"),
                    @ApiResponse(responseCode = "500", description = "Произошла внутреняя ошибка сервера")
            })
    public ResponseEntity<Void> syncLocalRepository(@RequestParam("localPath") String localPath,
                                                    @RequestParam("repoName") String repoName) {
        try {
            githubRepositorySynchronizationService.syncLocalRepository(localPath, repoName);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            log.error(e.getMessage());

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Непредвиденная ошибка: " + e.getMessage());

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/upload/all")
    @Operation(
            summary = "Загрузка репозиториев",
            description = "Позволяет загрузить репозитории с диска на Bitbucket"
    )

    public ResponseEntity<Void> uploadRepositories() {
        try {
            bitbucketService.uploadAllRepositoriesToBitbucket("C:\\idc");
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (URISyntaxException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @PutMapping("/upload")
    public ResponseEntity<Void> uploadRepository(@RequestParam("localPath") String localPath,
                                                 @RequestParam("repoName") String repoName) {
        try {
            bitbucketService.uploadRepositoryToBitbucket(localPath, repoName);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
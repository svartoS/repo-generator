package com.svarto.repo_generator.controller;

import com.svarto.repo_generator.config.ApplicationConfiguration;
import com.svarto.repo_generator.model.GitProvider;
import com.svarto.repo_generator.model.Repository;
import com.svarto.repo_generator.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
    @Value("${repository.source.provider}")
    private GitProvider sourceProvider;

    @Value("${repository.target.provider}")
    private GitProvider targetProvider;
    private final GitServiceFactory gitServiceFactory;

    public RepositoryController(GitServiceFactory gitServiceFactory) {
        this.gitServiceFactory = gitServiceFactory;
    }

    @GetMapping("/repositories")
    @Operation(summary = "Получение списка всех репозиториев с Github",
            description = "Возвращает список репозиториев",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Репозитории были успешно получены"),
                    @ApiResponse(responseCode = "500")
            })
    public ResponseEntity<List<Repository>> getRepositories() {
        try {
            List<Repository> repositories = gitServiceFactory.createSourceService(sourceProvider).getRepositories();
            return new ResponseEntity<>(repositories, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Ошибка при получении репозиториев: {}", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/sync/local/all")
    @Operation(
            summary = "Обновление репозиториев с Github",
            description = "Позволяет склонировать или обновить ВСЕ репозитории с Github на диск",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Репозитории были успешно обработаны"),
                    @ApiResponse(responseCode = "400", description = "Неверный запрос"),
                    @ApiResponse(responseCode = "500")
            })
    public ResponseEntity<Void> syncAllLocalRepositories(@RequestParam("localPath") String localPath) {
        if (localPath == null || localPath.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            gitServiceFactory.createSourceService(sourceProvider).syncAllLocalRepositories(localPath);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при синхронизации репозиториев: {}", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/sync/local/")
    @Operation(summary = "Обновление конкретного репозитория с Github",
            description = "Клонирует или обновляет конкретный репозиторий",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Репозиторий  был успешно обработан"),
                    @ApiResponse(responseCode = "400", description = "Не удалось найти данный репозиторий на диске"),
                    @ApiResponse(responseCode = "500")
            })
    public ResponseEntity<Void> syncLocalRepository(@RequestParam("localPath") String localPath,
                                                    @RequestParam("repoName") String repoName) {
        if (localPath == null || localPath.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            gitServiceFactory.createSourceService(sourceProvider).syncLocalRepository(localPath, repoName);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            log.error("Ошибка при синхронизации репозитория: {}", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при синхронизации репозиториея: {}", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/upload/all")
    @Operation(
            summary = "Загрузка репозиториев на Bitbucket",
            description = "Позволяет загрузить репозитории с диска на Bitbucket",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Репозитории были успешно обработаны"),
                    @ApiResponse(responseCode = "400", description = "Не удалось найти данные репозитории на диске"),
                    @ApiResponse(responseCode = "500")
            })
    public ResponseEntity<Void> uploadRepositories(@RequestParam("localPath") String localPath) {
        try {
            gitServiceFactory.createTargetService(targetProvider).uploadAllRepositories(localPath);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (URISyntaxException e) {
            log.error("Ошибка при загрузке репозиториев: {}", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при загрузке репозиториев: {}", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping("/upload")
    @Operation(
            summary = "Загрузка репозитория на Bitbucket",
            description = "Позволяет загрузить конкретный репозиторий с диска на Bitbucket",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Репозиторий  был успешно обработан"),
                    @ApiResponse(responseCode = "404", description = "Не удалось найти данный репозиторий"),
                    @ApiResponse(responseCode = "500")
            })
    public ResponseEntity<Void> uploadRepository(@RequestParam("localPath") String localPath,
                                                 @RequestParam("repoName") String repoName) {
        try {
            gitServiceFactory.createTargetService(targetProvider).uploadRepository(localPath, repoName);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error("Ошибка при загрузке репозитория: {}", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
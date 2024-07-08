package com.svarto.repo_generator.service;

import com.svarto.repo_generator.model.Repository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@NoArgsConstructor
public class GitService {
    private String username;
    private String accessToken;
    private UsernamePasswordCredentialsProvider credentialsProvider;

    public GitService(String gitUsername, String gitAccessToken) {
        this.username = gitUsername;
        this.accessToken = gitAccessToken;
        this.credentialsProvider = new UsernamePasswordCredentialsProvider(gitUsername, gitAccessToken);
    }
    public void cloneRepository(Repository repository) throws GitAPIException {
        try {
            Git git = Git.cloneRepository()
                    .setURI(repository.getCloneUrl())
                    .setDirectory(new File(repository.getLocalPath()))
                    .setCloneAllBranches(true)
                    .setCredentialsProvider(credentialsProvider).call();

            updateBranch(git);

            log.info("Репозиторий " + repository.getCloneUrl() + " был успешно склонирован в "
                             + repository.getLocalPath());
        } catch (Exception e) {
            log.error("Ошибка клонирования репозитория: {}", e);
            throw new RuntimeException(e);
        }
    }

    public void updateRepository(Repository repository) {
        try {
            Git git = Git.open(new File(repository.getLocalPath()));
            PullCommand pullCommand = git.pull()
                    .setCredentialsProvider(credentialsProvider);
            pullCommand.call();

            updateBranch(git);

            log.info("Репозиторий " + repository.getLocalPath() + " был успешно обновлен");
        } catch (Exception e) {
            log.error("Ошибка обновления репозитория: {}", e);
            throw new RuntimeException(e);
        }
    }

    public void updateBranch(Git git) {
        try {
            log.info("Получение обновлений с удаленного репозитория");
            git.fetch()
                    .setCredentialsProvider(credentialsProvider)
                    .setRemoveDeletedRefs(true)
                    .call();

            List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

            for (Ref remoteBranch : branches) {
                String remoteBranchName = remoteBranch.getName();
                String localBranchName = remoteBranchName.substring(remoteBranchName.lastIndexOf('/') + 1);

                boolean branchExists = git.branchList().call().stream()
                        .map(Ref::getName)
                        .anyMatch(name -> name.equals("refs/heads/" + localBranchName));

                if (!branchExists) {
                    log.info("Создание локальной ветки: {}", localBranchName);

                    git.checkout()
                            .setCreateBranch(true)
                            .setName(localBranchName)
                            .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                            .setStartPoint(remoteBranchName)
                            .call();
                }
            }
            log.info("Локальные ветки были успешно созданы");
        } catch (GitAPIException e) {
            log.error("Произошла ошибка во время создания локальный веток: {}", e);
        }
    }
}

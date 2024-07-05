package com.svarto.repo_generator.service;

import com.svarto.repo_generator.model.Repository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GitService {
    @Value("${github.username}")
    private String username;

    @Value("${github.accessToken}")
    private String accessToken;

    public void cloneRepository(Repository repository) throws GitAPIException {
        try {
            Git git = Git.cloneRepository()
                    .setURI(repository.getClone_url())
                    .setDirectory(new File(repository.getLocalPath()))
                    .setCloneAllBranches(true)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, accessToken)).call();

            updateBranch(repository, git);

            log.info("Репозиторий " + repository.getClone_url() + "был успешно склонирован в "
                             + repository.getLocalPath());
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void updateRepository(Repository repository) {
        try {
            Git git = Git.open(new File(repository.getLocalPath()));
            PullCommand pullCommand = git.pull()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, accessToken));
            pullCommand.call();

            updateBranch(repository, git);
            log.info("Репозиторий " + repository.getLocalPath() + " был успешно обновлен");
        } catch (Exception e) {
            log.error("Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void updateBranch(Repository repository, Git git) throws GitAPIException {
        git.fetch()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, accessToken))
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
                log.info("Creating and checking out local branch: " + localBranchName);

                git.checkout()
                        .setCreateBranch(true)
                        .setName(localBranchName)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .setStartPoint(remoteBranchName)
                        .call();
            } else {
                log.warn("Local branch already exists: " + localBranchName);
            }
        }
    }

    public void pushRepository(Repository repository) {
        try {
            Git git = Git.open(new File(repository.getLocalPath()));
            git.push().call();
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getBranches(String localPath) throws GitAPIException, IOException {
        Git git = Git.open(new File(localPath));
        List<String> branches = new ArrayList<>();
        git.branchList().call().forEach(branch -> branches.add(branch.getName()));
        return branches;
    }

}

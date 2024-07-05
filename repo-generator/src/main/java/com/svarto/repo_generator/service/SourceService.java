package com.svarto.repo_generator.service;

import com.svarto.repo_generator.model.Branch;
import com.svarto.repo_generator.model.Repository;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;

public interface SourceService {
    public List<Repository> getRepositories();
    public Repository getRepository(String repoName);
    public List<Branch> getBranches(String repoUrl);
}

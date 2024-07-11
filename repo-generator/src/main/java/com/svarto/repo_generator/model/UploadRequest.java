package com.svarto.repo_generator.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UploadRequest {
    private String localPath;
    private String repoName;

}
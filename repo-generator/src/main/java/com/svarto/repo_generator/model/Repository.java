package com.svarto.repo_generator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {
    private Long id;
    private String name;
    private String full_name;
    private String html_url;
    private String description;
    private String url;
    private String ssh_url;
    private String clone_url;
    private String svn_url;
    private String default_branch;
    private String branches_url;
    private String localPath;
    private GitProvider provider;


    private List<Branch> branches;

}
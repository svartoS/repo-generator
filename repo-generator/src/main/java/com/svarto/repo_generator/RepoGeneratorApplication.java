package com.svarto.repo_generator;

import com.svarto.repo_generator.config.BitbucketProperties;
import com.svarto.repo_generator.config.GitHubProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({GitHubProperties.class, BitbucketProperties.class})
@SpringBootApplication
public class RepoGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoGeneratorApplication.class, args);
    }

}

package com.svarto.repo_generator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {
    private final GitHubProperties gitHubProperties;
    private final BitbucketProperties bitbucketProperties;

    public ApplicationConfiguration(GitHubProperties gitHubProperties, BitbucketProperties bitbucketProperties) {
        this.gitHubProperties = gitHubProperties;
        this.bitbucketProperties = bitbucketProperties;
    }


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                              .title("Your API")
                              .version("1.0")
                              .description("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
    }

    @Bean
    public UsernamePasswordCredentialsProvider bitbucketCredentialsProvider(){
        return new UsernamePasswordCredentialsProvider(bitbucketProperties.getUsername(), bitbucketProperties.getPassword());
    }
}


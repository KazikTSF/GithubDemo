package com.kazmierczak.GithubDemo.service;

import com.kazmierczak.GithubDemo.dto.BranchResponse;
import com.kazmierczak.GithubDemo.dto.RepoResponse;
import com.kazmierczak.GithubDemo.exception.UserNotFoundException;
import com.kazmierczak.GithubDemo.models.Branch;
import com.kazmierczak.GithubDemo.models.Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubService {
    @Value("${apiKey}")
    private String apiKey;

    private final WebClient webClient;

    public Flux<RepoResponse> getAllRepos(String userLogin) {
        return webClient.get()
                .uri("/users/{userLogin}/repos", userLogin)
                .headers(h -> h.setBearerAuth(apiKey))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new UserNotFoundException("User not found: " + userLogin)))
                .bodyToFlux(Repository.class)
                .filter(r -> !r.fork())
                .flatMap(this::mapToRepoResponse)
                .limitRate(10)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(t -> !(t instanceof UserNotFoundException)));
    }

    private Mono<RepoResponse> mapToRepoResponse(Repository repository) {
        return getBranches(repository.owner().login(), repository.name()).collectList()
                .map(branches -> new RepoResponse(repository.name(), repository.owner().login(), branches));
    }

    private Flux<BranchResponse> getBranches(String ownerLogin, String repoName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/repos/{ownerLogin}/{repoName}/branches")
                        .build(ownerLogin, repoName))
                .headers(h -> h.setBearerAuth(apiKey))
                .retrieve()
                .bodyToFlux(Branch.class)
                .map(branch -> new BranchResponse(branch.name(), branch.commit().sha()));
    }
}

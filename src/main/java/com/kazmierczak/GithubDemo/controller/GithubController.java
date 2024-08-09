package com.kazmierczak.GithubDemo.controller;

import com.kazmierczak.GithubDemo.dto.RepoResponse;
import com.kazmierczak.GithubDemo.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/repos")
@RequiredArgsConstructor
public class GithubController {
    private final GithubService githubService;
    @GetMapping(value = "/{user-login}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Flux<RepoResponse> getAllRepos(@PathVariable("user-login") String userLogin) {
        return githubService.getAllRepos(userLogin);
    }
}

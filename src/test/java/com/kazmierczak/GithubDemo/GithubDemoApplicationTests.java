package com.kazmierczak.GithubDemo;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.kazmierczak.GithubDemo.dto.BranchResponse;
import com.kazmierczak.GithubDemo.dto.RepoResponse;
import com.kazmierczak.GithubDemo.exception.UserNotFoundException;
import com.kazmierczak.GithubDemo.models.Branch;
import com.kazmierczak.GithubDemo.models.Commit;
import com.kazmierczak.GithubDemo.models.Owner;
import com.kazmierczak.GithubDemo.models.Repository;
import com.kazmierczak.GithubDemo.service.GithubService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpsEnabled = true)
@AutoConfigureWebTestClient
class GithubDemoApplicationTests {
    @Autowired
    private GithubService githubService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Test
    @DisplayName("Should Throw User not Found exception")
    public void getAllRepos_UserNotFoundException(WireMockRuntimeInfo wmRuntimeInfo) {
        String baseUrl = "http://localhost:" + wmRuntimeInfo.getHttpPort();
        githubService = new GithubService(WebClient.builder().baseUrl(baseUrl).build());
        stubFor(get(urlEqualTo("/users/testError/repos")).willReturn(aResponse()
                .withBody("{\"message\":\"Not Found\",\"documentation_url\":\"https://docs.github.com/rest/repos/repos#list-repositories-for-a-user\",\"status\":\"404\"}")
                .withStatus(404)
                .withHeader("Content-Type", "application/json")));
        Flux<RepoResponse> response = githubService.getAllRepos("testError");
        StepVerifier.create(response)
                .expectErrorMatches(t -> t instanceof UserNotFoundException &&  t.getMessage().equals("User not found: testError"))
                .verify();
    }
    @Test
    @DisplayName("Should return user repositories")
    public void getAllRepos_OK(WireMockRuntimeInfo wmRuntimeInfo) {
        String baseUrl = "http://localhost:" + wmRuntimeInfo.getHttpPort();
        List<Repository> repos = new ArrayList<>();
        repos.add(getFork());
        repos.add(getRepository());

        githubService = new GithubService(WebClient.builder().baseUrl(baseUrl).build());
        stubFor(get(urlPathMatching("/users/testOwner/repos")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withJsonBody(objectMapper.valueToTree(repos))
                .withStatus(200)));
        stubFor(get(urlPathMatching("/repos/testOwner/testRepo/branches")).willReturn(aResponse()
                .withJsonBody(objectMapper.valueToTree(getBranches()))
                .withStatus(200)
                .withHeader("Content-Type", "application/json")));
        Flux<RepoResponse> response = githubService.getAllRepos("testOwner");
        assertThat(response).isNotNull();
        List<RepoResponse> responses = response.collectList().block();
        assertThat(responses).isNotNull();
        assertThat(responses.size()).isEqualTo(1);
        assertThat(responses.getFirst().getName()).isEqualTo("testRepo");
        assertThat(responses.getFirst().getOwnerLogin()).isEqualTo("testOwner");
        List<BranchResponse> branches = responses.getFirst().getBranches();
        assertThat(branches).isNotNull();
        assertThat(branches.get(0).getName()).isEqualTo("testBranch1");
        assertThat(branches.get(1).getName()).isEqualTo("testBranch2");

    }
    private Repository getRepository() {
        Owner owner = new Owner("testOwner");
        return new Repository("testRepo", owner, false);
    }
    private Repository getFork() {
        Owner owner = new Owner("testOwner");
        return new Repository("testFork", owner, true);
    }

    private List<Branch> getBranches() {
        List<Branch> branches = new ArrayList<>();

        Commit commit1 = new Commit("sha1");
        Branch branch1 = new Branch("testBranch1", commit1);

        Commit commit2 = new Commit("sha2");
        Branch branch2 = new Branch("testBranch2", commit2);

        branches.add(branch1);
        branches.add(branch2);

        return branches;
    }
}

package com.kazmierczak.GithubDemo.dto;

import java.util.List;

public record RepoResponse(String name, String ownerLogin, List<BranchResponse> branches) {
}

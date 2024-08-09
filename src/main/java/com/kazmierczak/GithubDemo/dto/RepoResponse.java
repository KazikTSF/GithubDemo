package com.kazmierczak.GithubDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepoResponse {
    private String name;
    private String ownerLogin;
    private List<BranchResponse> branches;
}

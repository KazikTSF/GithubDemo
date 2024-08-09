package com.kazmierczak.GithubDemo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Repository {
    private String name;
    private Owner owner;
    boolean fork;
}

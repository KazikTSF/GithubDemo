package com.kazmierczak.GithubDemo.models;

public record Repository(String name, Owner owner, boolean fork) {}

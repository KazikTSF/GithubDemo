package com.kazmierczak.GithubDemo.exception;

public record ErrorResponse(int status, String message) {
}

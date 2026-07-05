package kr.ktb.finn_week6.global;

import lombok.Getter;

@Getter
public enum RequestMessage {
    SUCCESS("request_success"),
    DUPLICATE_EMAIL("Email already exists"),
    NOT_FOUND("Not found"),
    INVALID_EMAIL("Invalid email format"),
    INVALID_PASSWORD("Invalid password"),
    UNAUTHORIZED("Unauthorized"),
    FORBIDDEN("Forbidden"),
    NOT_FOUND_USER("User not found"),
    NOT_FOUND_POST("Post not found"),
    NOT_FOUND_EMAIL("Email not found"),
    RESOURCE_DELETED("Resource deleted"),
    ALREADY_LIKED("Already liked"),
    TOKEN_REFRESH_SUCCESS("Token refreshed successfully");

    private final String description;

    RequestMessage(String description) {
        this.description = description;
    }

}

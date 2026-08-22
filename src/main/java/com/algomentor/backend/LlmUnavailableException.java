package com.algomentor.backend;

/** Raised when the upstream model provider could not be reached or refused the request. */
public class LlmUnavailableException extends RuntimeException {

    private final String userMessage;

    public LlmUnavailableException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
    }

    /** Safe to show to the caller: contains no key material or raw provider payload. */
    public String getUserMessage() {
        return userMessage;
    }
}

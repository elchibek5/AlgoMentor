package com.algomentor.backend;

public class ChatResponse {

    private final String answer;

    public ChatResponse(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }
}

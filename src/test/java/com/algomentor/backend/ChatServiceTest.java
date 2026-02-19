package com.algomentor.backend;

import com.algomentor.backend.llm.LlmClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    @Test
    void returnsTrimmedLlmResponse() {
        LlmClient llmClient = mock(LlmClient.class);
        ChatService service = new ChatService(llmClient);

        ChatRequest request = new ChatRequest();
        request.setQuestion("How can I optimize this?");
        request.setLanguage("java");

        when(llmClient.chat(org.mockito.ArgumentMatchers.anyString())).thenReturn("  Use a hash map.  ");

        ChatResponse response = service.ask(request);

        assertEquals("Use a hash map.", response.getAnswer());
        verify(llmClient).chat(contains("How can I optimize this?"));
    }
}

package com.algomentor.backend;

import com.algomentor.backend.llm.LlmClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Test
    void promptIncludesInteractiveAndPersonalizedGuidance() {
        LlmClient llmClient = mock(LlmClient.class);
        ChatService service = new ChatService(llmClient);

        ChatRequest request = new ChatRequest();
        request.setQuestion("Can you review my two-pointer approach?");
        request.setLanguage("python");
        request.setProblem("Two Sum II");
        request.setContext("I get nervous in interviews and rush edge cases");

        when(llmClient.chat(anyString())).thenReturn("Plan looks good");

        service.ask(request);

        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(llmClient).chat(captor.capture());

        String prompt = captor.getValue();
        assertTrue(prompt.contains("Response format:"));
        assertTrue(prompt.contains("One follow-up coaching question"));
        assertTrue(prompt.contains("extra_context_present: true"));
        assertTrue(prompt.contains("I get nervous in interviews"));
    }
}

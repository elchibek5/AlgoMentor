package com.algomentor.backend;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Chat", description = "Interactive mentoring chat endpoints")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/api/chat")
    @Operation(
            summary = "Ask the mentor a follow-up question",
            description = "Get personalized guidance on a solution with adaptive coaching tone and practical next steps.",
            tags = {"Chat"}
    )
    @ApiResponse(responseCode = "200", description = "Chat response generated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Invalid request (validation failed)")
    @ApiResponse(responseCode = "500", description = "Unexpected server error")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.ask(request);
    }
}

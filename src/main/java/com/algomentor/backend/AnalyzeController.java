package com.algomentor.backend;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Analysis", description = "Algorithm solution analysis endpoints")
public class AnalyzeController {

    private final AnalyzeService service;

    public AnalyzeController(AnalyzeService service) {
        this.service = service;
    }

    @PostMapping("/api/analyze")
    @Operation(
            summary = "Analyze an algorithm solution",
            description = "Submits a code solution for comprehensive analysis covering correctness, complexity, edge cases, pitfalls, tests, and improvements.",
            tags = {"Analysis"}
    )
    @ApiResponse(responseCode = "200", description = "Analysis completed successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Invalid request (validation failed)")
    @ApiResponse(responseCode = "502", description = "LLM returned invalid response after retries")
    @ApiResponse(responseCode = "500", description = "Unexpected server error")
    public AnalyzeResponse analyze(@Valid @RequestBody AnalyzeRequest req) {
        return service.analyze(req);
    }
}

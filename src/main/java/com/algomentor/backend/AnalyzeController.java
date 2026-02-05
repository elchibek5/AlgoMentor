package com.algomentor.backend;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class AnalyzeController {

    private final AnalyzeService service;

    public AnalyzeController(AnalyzeService service) {
        this.service = service;
    }

    @PostMapping("/api/analyze")
    public AnalyzeResponse analyze(@Valid @RequestBody AnalyzeRequest req) {
        return service.analyze(req);
    }
}

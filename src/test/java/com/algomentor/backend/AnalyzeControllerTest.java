package com.algomentor.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyzeController.class)
class AnalyzeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AnalyzeService service;


    @Test
    void missingSolution_returns400() throws Exception {
        String body = """
        {
          "language": "java",
          "mode": "interview"
        }
        """;

        mvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidMode_returns400() throws Exception {
        String body = """
        {
          "language": "java",
          "solution": "class Solution {}",
          "mode": "expert"
        }
        """;

        mvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }


    @Test
    void invalidModelOutput_returns502() throws Exception {
        doThrow(new InvalidModelOutputException("bad output")).when(service).analyze(any());

        String body = """
        {
          "language": "java",
          "solution": "class Solution {}",
          "mode": "interview"
        }
        """;

        mvc.perform(post("/api/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("invalid_model_output"));
    }

}

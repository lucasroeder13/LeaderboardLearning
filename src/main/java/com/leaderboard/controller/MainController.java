package com.leaderboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Health Check", description = "API health monitoring endpoints")
public class MainController {

    @Operation(summary = "Health check", description = "Check if the API is running")
    @GetMapping("/health")
    public String test(){
        log.debug("Health check endpoint accessed");
        return "alive";
    }
}
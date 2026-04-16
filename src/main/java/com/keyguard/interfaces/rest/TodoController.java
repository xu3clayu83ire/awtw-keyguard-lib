package com.keyguard.interfaces.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/todolist")
public class TodoController {

    @GetMapping("/{loginId}")
    public Map<String, Object> getTodoList(
            @PathVariable String loginId,
            @RequestAttribute("keyguard.vendorName") String vendorName,
            @RequestAttribute("keyguard.apiKeyId") Long apiKeyId
    ) {
        return Map.of(
                "loginId", loginId,
                "vendorName", vendorName,
                "apiKeyId", apiKeyId,
                "message", "API key validated"
        );
    }
}
package com.project.yogerOrder.global.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommonController {

    @GetMapping("/health")
    public String hello() {
        return "Yoger order Server is alive";
    }
}

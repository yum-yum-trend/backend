package com.udangtangtang.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/")
    public String forHealthCheck() {
        return "OK from Docker Server";
    }

    @GetMapping(value = "/error/test/CyhDZOquoh")
    public String forErrorTest() {
        int i = 1/0;
        return "FOR ERROR TEST";
    }
}
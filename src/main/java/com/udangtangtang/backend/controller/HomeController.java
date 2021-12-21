package com.udangtangtang.backend.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping(value = "/")
    public String forHealthCheck(Model model) {
        return "index";
    }

    @GetMapping(value = "/error/test/CyhDZOquoh")
    public String forErrorTest() {
        int i = 1/0;
        return "FOR ERROR TEST";
    }
}
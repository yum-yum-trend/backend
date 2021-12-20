package com.udangtangtang.backend.controller;

import com.udangtangtang.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public List<String> getSearchTags() {
        return searchService.getTagList();
    }
}

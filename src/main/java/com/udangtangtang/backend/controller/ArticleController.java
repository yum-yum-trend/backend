package com.udangtangtang.backend.controller;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.Location;
import com.udangtangtang.backend.dto.LocationRequestDto;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/articles")
    public List<Article> getArticles() {
        return articleService.getArticles();
    }

    @GetMapping("/articles/{id}")
    public Article getArticle(@PathVariable Long id) {
        return articleService.getArticle(id);
    }
    @PostMapping("/articles")
    public void createArticle(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @RequestParam("text") String text,
                                @RequestParam("location") String location,
                                @RequestParam("hashtagNameList") List<String> hashtagNameList,
                                @RequestParam("imageFileList") List<MultipartFile> imageFileList) {
        System.out.println(111111);
        JSONObject rjson = new JSONObject(location);
        System.out.println(rjson);
        System.out.println(rjson.getJSONObject("xCoordinate"));
        articleService.createArticle(userDetails.getUser(), text, "ddd", hashtagNameList, imageFileList);
    }
}

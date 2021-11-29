package com.udangtangtang.backend.controller;

import com.udangtangtang.backend.dto.LikeResponseDto;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @GetMapping("/likes")
    public List<LikeResponseDto> getLikes(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return likeService.getLikes(userDetails.getUser().getId());
    }

    @GetMapping("/likes/{id}")
    public LikeResponseDto getArticle(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return likeService.getLike(id, userDetails.getUser().getId());
    }

    @PutMapping("/articles/like")
    public void increaseLikeCount(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                  @RequestParam("articleId") Long articleId) {
        likeService.increaseLikeCount(userDetails.getId(), articleId);
    }

    @PutMapping("articles/unlike")
    public void decreaseLikeCount(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                  @RequestParam("articleId") Long articleId) {
        likeService.decreaseLikeCount(userDetails.getId(), articleId);
    }
}

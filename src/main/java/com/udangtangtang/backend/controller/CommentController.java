package com.udangtangtang.backend.controller;

import com.udangtangtang.backend.domain.Comment;
import com.udangtangtang.backend.dto.CommentRequestDto;
import com.udangtangtang.backend.dto.CommentResponseDto;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping(value = "/comment/{articleId}")
    public List<CommentResponseDto> showArticleComments(@PathVariable("articleId") Long articleId) {
        return commentService.getArticleComments(articleId);
    }

    @PostMapping(value = "/comment/{articleId}")
    public void saveComment(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable("articleId") Long articleId, @RequestBody CommentRequestDto commentRequestDto) {
        commentService.saveComment(userDetails.getId(), articleId, commentRequestDto);
    }

    @DeleteMapping(value = "/comment/{commentId}")
    public void deleteComment(@PathVariable("commentId") Long commentId) {
        commentService.deleteComment(commentId);
    }
}

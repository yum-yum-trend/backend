package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.Comment;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.dto.request.CommentRequestDto;
import com.udangtangtang.backend.dto.response.CommentResponseDto;
import com.udangtangtang.backend.exception.ApiRequestException;
import com.udangtangtang.backend.repository.ArticleRepository;
import com.udangtangtang.backend.repository.CommentRepository;
import com.udangtangtang.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public List<CommentResponseDto> getArticleComments(Long articleId) {
        List<Comment> ArticleComments = commentRepository.findAllByArticleId(articleId);
        List<CommentResponseDto> commentResponseDtos = new ArrayList<>();

        for (Comment comment : ArticleComments) {
            Long userId = comment.getUser().getId();
            String username = comment.getUser().getUsername();
            String userProfileImageUrl = comment.getUser().getUserProfileImageUrl();
            Long commentId = comment.getId();
            String commentText = comment.getCommentText();
            commentResponseDtos.add(new CommentResponseDto(userId, username, userProfileImageUrl, commentId, commentText));
        }

        return commentResponseDtos;
    }

    public void saveComment(Long userId, Long articleId, CommentRequestDto commentRequestDto) {
        Article article = articleRepository.findById(articleId).orElseThrow(
                () -> new ApiRequestException("해당 게시글이 존재하지 않습니다."));
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApiRequestException("해당 유저가 존재하지 않습니다."));
        Comment comment = new Comment(user, article, commentRequestDto.getCommentText());
        commentRepository.save(comment);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}

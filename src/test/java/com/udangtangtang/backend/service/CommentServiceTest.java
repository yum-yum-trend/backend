package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.Comment;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.exception.ApiRequestException;
import com.udangtangtang.backend.repository.ArticleRepository;
import com.udangtangtang.backend.repository.CommentRepository;
import com.udangtangtang.backend.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class) // 스프링과 테스트 통합
@SpringBootTest // 스프링 부트 띄우고 테스트(이게 없으면 @Autowired 다 실패)
@Transactional
public class CommentServiceTest {

    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    CommentService commentService;

    @Test
    public void 댓글_등록() {
        // given
        Article article = articleRepository.findById(3L).orElseThrow(
                () -> new ApiRequestException("해당 게시글이 존재하지 않습니다."));
        User user = userRepository.findById(1L).orElseThrow(
                () -> new ApiRequestException("해당 유저가 존재하지 않습니다."));
        Comment comment = new Comment(user, article, "test 댓글입니다.");

        // when
        commentRepository.save(comment);

        // then
        assertEquals(comment, commentRepository.findById(comment.getId()).orElseThrow(
                () -> new ApiRequestException("해당 댓글이 존재하지 않습니다.")));
    }

    @Test(expected = ApiRequestException.class)
    public void 댓글_삭제() {
        // given
        Article article = articleRepository.findById(3L).orElseThrow(
                () -> new ApiRequestException("해당 게시글이 존재하지 않습니다."));
        User user = userRepository.findById(1L).orElseThrow(
                () -> new ApiRequestException("해당 유저가 존재하지 않습니다."));
        Comment comment = new Comment(user, article, "test 댓글입니다.");
        commentRepository.save(comment);
        Long commentId = comment.getId();

        // when
        commentService.deleteComment(commentId);

        // then
        commentRepository.findById(commentId).orElseThrow(
                () -> new ApiRequestException("해당 댓글이 존재하지 않습니다."));
    }
}
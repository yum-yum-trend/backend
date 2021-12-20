package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.*;
import com.udangtangtang.backend.dto.request.LocationRequestDto;
import com.udangtangtang.backend.exception.ApiRequestException;
import com.udangtangtang.backend.repository.CommentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class) // 스프링과 테스트 통합
@SpringBootTest // 스프링 부트 띄우고 테스트(이게 없으면 @Autowired 다 실패)
@Transactional
public class CommentServiceTest {
    
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    CommentService commentService;

    @Test
    public void 댓글_등록과_조회() {
        // given
        User user = createUser("testUser", "testPw", "test@test.com", UserRole.USER);
        Article article = createArticle("testText", user);
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
        User user = createUser("testUser", "testPw", "test@test.com", UserRole.USER);
        Article article = createArticle("testText", user);
        Comment comment = new Comment(user, article, "test 댓글입니다.");
        commentRepository.save(comment);
        Long commentId = comment.getId();

        // when
        commentService.deleteComment(commentId);

        // then
        commentRepository.findById(commentId).orElseThrow(
                () -> new ApiRequestException("해당 댓글이 존재하지 않습니다."));
    }

    public User createUser(String username, String password, String email, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    public Article createArticle(String text, User user) {
        Article article = new Article();
        LocationRequestDto locationRequestDto = new LocationRequestDto();
        Location location = new Location(locationRequestDto, user.getId());
        article.setText(text);
        article.setUser(user);
        article.setLocation(location);
        return article;
    }
}
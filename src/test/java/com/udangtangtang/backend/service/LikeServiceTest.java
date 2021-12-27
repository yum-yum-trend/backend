package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.dto.request.ArticleCreateRequestDto;
import com.udangtangtang.backend.dto.request.LocationRequestDto;
import com.udangtangtang.backend.dto.request.SignupRequestDto;
import com.udangtangtang.backend.dto.response.LikeResponseDto;
import com.udangtangtang.backend.repository.ArticleRepository;
import com.udangtangtang.backend.repository.LikesRepository;
import com.udangtangtang.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@Transactional
@SpringBootTest
class LikeServiceTest {

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LikesRepository likesRepository;

    @Autowired
    ArticleService articleService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    LikeService likeService;

    @Test
    void 게시글_좋아요_추가_성공() throws Exception {
        //given
        SignupRequestDto signupRequestDto = createSignupRequestDto("testtest", "123", "testtest@test.com");
        userService.createUser(signupRequestDto);
        User user = userRepository.findByUsername(signupRequestDto.getUsername()).orElseThrow(
                () -> new NullPointerException("해당 유저가 존재하지 않습니다.")
        );
        String text = "아무소리";
        String location = "{}";
        List<String> tagsName = new ArrayList<>();
        List<Long> imageIds = new ArrayList<>();
        Article article = articleService.createArticle(user, new ArticleCreateRequestDto(text, location, tagsName, imageIds));

        //when
        likeService.increaseLikeCount(user.getId(), article.getId());

        //then
        assertEquals(user.getId(), likesRepository.findByUserId(user.getId()).getUserId());
        assertEquals(article.getId(), likesRepository.findByArticleId(article.getId()).getArticleId());
    }

    @Test
    void 게시글_좋아요_제거_성공() throws Exception {
        //given
        SignupRequestDto signupRequestDto = createSignupRequestDto("testtest", "123", "testtest@test.com");
        userService.createUser(signupRequestDto);
        User user = userRepository.findByUsername(signupRequestDto.getUsername()).orElseThrow(
                () -> new NullPointerException("해당 유저가 존재하지 않습니다.")
        );

        String text = "아무소리";
        String location = "{}";
        List<String> tagsNames = new ArrayList<>();
        List<Long> imageIds = new ArrayList<>();
        Article article = articleService.createArticle(user, new ArticleCreateRequestDto(text, location, tagsNames, imageIds));
        likeService.increaseLikeCount(user.getId(), article.getId());

        //when
        Long articleId = likeService.decreaseLikeCount(user.getId(), article.getId());
        likesRepository.findByArticleId(article.getId());

        //then
        assertEquals(article.getId(), articleId);
    }

    @Test
    void 해당_게시글_좋아요_갯수_좋아요_여부_확인() throws Exception {
        //given
        SignupRequestDto signupRequestDto1 = createSignupRequestDto("testtest", "123", "testtest@test.com");
        userService.createUser(signupRequestDto1);
        User user1 = userRepository.findByUsername(signupRequestDto1.getUsername()).orElseThrow(
                () -> new NullPointerException("해당 유저가 존재하지 않습니다.")
        );
        SignupRequestDto signupRequestDto2 = createSignupRequestDto("testtest2", "123", "testtest2@test.com");
        userService.createUser(signupRequestDto2);
        User user2 = userRepository.findByUsername(signupRequestDto2.getUsername()).orElseThrow(
                () -> new NullPointerException("해당 유저가 존재하지 않습니다.")
        );

        String text = "아무소리";
        String location = "{}";
        List<String> tagsNames = new ArrayList<>();
        List<Long> imageIds = new ArrayList<>();
        Article article = articleService.createArticle(user1, new ArticleCreateRequestDto(text, location, tagsNames, imageIds));

        likeService.increaseLikeCount(user1.getId(), article.getId());
        likeService.increaseLikeCount(user2.getId(), article.getId());

        //when
        LikeResponseDto dto1 = likeService.getLike(article.getId(), user1.getId());
        LikeResponseDto dto2 = likeService.getLike(article.getId(), user2.getId());

        //then
        assertEquals(2, dto1.getLikeCount());
        assertEquals(true, dto1.isLike());
        assertEquals(2, dto2.getLikeCount());
        assertEquals(true, dto2.isLike());
    }

    private SignupRequestDto createSignupRequestDto(String username, String password, String email) {
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUsername(username);
        signupRequestDto.setPassword(password);
        signupRequestDto.setEmail(email);
        return signupRequestDto;
    }
}
package com.udangtangtang.backend.controller;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.ArticleService;
import com.udangtangtang.backend.service.UserProfileService;
import com.udangtangtang.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping(value = "/profile/{userId}")
    public Optional<User> getUserProfileInfo(@PathVariable("userId") Long userId) {
        return userProfileService.getUserProfileInfo(userId);
    }

    @GetMapping(value = "/profile/articles/{userId}")
    public List<Article> getUserArticles(@PathVariable("userId") Long userId) {
        return userProfileService.getUserArticles(userId);
    }

    @PostMapping(value = "/profile/imagechange/{userId}")
    public String getProfileImage(@PathVariable("userId") Long userId,
                                @RequestParam("newProfileImage") MultipartFile newProfileImage) {
        System.out.println("여기?");
        String url = userProfileService.updateProfileImage(userId, newProfileImage);
        return url;
    }


//    @GetMapping(value = "/profile/comments/${userId}")
//    public List<Comment> getUserComments(@PathVariable("userId") Long userId) {
//        return userProfileService.getUserComments(username);
//    }
}

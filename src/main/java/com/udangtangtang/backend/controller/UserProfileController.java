package com.udangtangtang.backend.controller;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.dto.ProfileRequestDto;
import com.udangtangtang.backend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping(value = "/profile/navbar-image/{userId}")
    public String getUserProfileImageUrl(@PathVariable("userId") Long userId) {
        return userProfileService.getUserProfileImageUrl(userId);
    }

    @GetMapping(value = "/profile/{userId}")
    public Optional<User> getUserProfileInfo(@PathVariable("userId") Long userId) {
        return userProfileService.getUserProfileInfo(userId);
    }

    @GetMapping(value = "/profile/articles/{userId}")
    public List<Article> getUserArticles(@PathVariable("userId") Long userId) {
        return userProfileService.getUserArticles(userId);
    }

//    @GetMapping(value = "/profile/bookmarks/{userId}")
//    public List<Article> getUserArticles(@PathVariable("userId") Long userId) {
//        return userProfileService.getUserBookmarks(userId);
//    }

    @PostMapping(value = "/profile/image-change/{userId}")
    public String getProfileImage(@PathVariable("userId") Long userId,
                                @RequestParam("newProfileImage") MultipartFile newProfileImage) {
        String url = userProfileService.updateProfileImage(userId, newProfileImage);
        return url;
    }

    @PutMapping(value = "/profile/{userId}")
    public void updateUserProfileInfo(@PathVariable("userId") Long userId,
                                          @RequestBody ProfileRequestDto profileRequestDto) throws Exception {
        userProfileService.updateUserProfileInfo(userId, profileRequestDto);
    }

    @DeleteMapping(value = "/profile/{userId}")
    public String resetUserProfileImage(@PathVariable("userId") Long userId) {
        userProfileService.resetUserProfileImage(userId);
        return "success";
    }
}

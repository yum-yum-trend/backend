package com.udangtangtang.backend.controller;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.dto.request.ProfileRequestDto;
import com.udangtangtang.backend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public Page<Article> getUserArticles(@PathVariable("userId") Long userId,
                                         @RequestParam("sortBy") String sortBy,
                                         @RequestParam("isAsc") boolean isAsc,
                                         @RequestParam("currentPage") int page ) {
        return userProfileService.getUserArticles(userId, sortBy, isAsc, page);
    }

    @GetMapping(value = "/profile/bookmarks/{userId}")
    public List<Object> getUserBookmarks(@PathVariable("userId") Long userId,
                                          @RequestParam("sortBy") String sortBy,
                                          @RequestParam("isAsc") boolean isAsc,
                                          @RequestParam("currentPage") int page ) {
        return userProfileService.getUserBookmarks(userId, sortBy, isAsc, page);
    }

    @PostMapping(value = "/profile/image-change/{userId}")
    public String getProfileImage(@PathVariable("userId") Long userId,
                                @RequestParam("newProfileImage") MultipartFile newProfileImage) {
        String url = userProfileService.updateProfileImage(userId, newProfileImage);
        return url;
    }

    @PutMapping(value = "/profile/pw/{userId}")
    public void updateUserPassword(@PathVariable("userId") Long userId,
                                          @RequestBody ProfileRequestDto profileRequestDto) throws Exception {
        userProfileService.updateUserPassword(userId, profileRequestDto);
    }

    @PostMapping(value = "/profile/intro/{userId}")
    public String updateUserProfileIntroText(@PathVariable("userId") Long userId,
                                             @RequestBody ProfileRequestDto profileRequestDto) throws Exception {
        return userProfileService.updateUserProfileIntroText(userId, profileRequestDto);
    }

    @DeleteMapping(value = "/profile/{userId}")
    public String resetUserProfileImage(@PathVariable("userId") Long userId) {
        userProfileService.resetUserProfileImage(userId);
        return "success";
    }
}

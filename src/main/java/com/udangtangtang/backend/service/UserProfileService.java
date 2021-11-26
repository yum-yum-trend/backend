package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.FileFolder;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.domain.UserRole;
import com.udangtangtang.backend.dto.ProfileChangesDto;
import com.udangtangtang.backend.repository.ArticleRepository;
import com.udangtangtang.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final FileProcessService fileProcessService;
    private final ArticleRepository articleRepository;

    public Optional<User> getUserProfileInfo(Long userId) {
        return userRepository.findById(userId);
    }

    public List<Article> getUserArticles(Long userId) {
        return articleRepository.findAllByUserId(userId);
    }

    @Transactional
    public String updateProfileImage(Long userId, MultipartFile newProfileImage) {
        Optional<User> user = userRepository.findById(userId);
        String url = fileProcessService.uploadImage(newProfileImage, FileFolder.PROFILE_IMAGES);
        user.get().setUserProfileImageUrl(url);
        return user.get().getUserProfileImageUrl();
    }

    public String getUserProfileImageUrl(Long userId) {
        return userRepository.findById(userId).get().getUserProfileImageUrl();
    }

    @Transactional
    public String updateUserProfileInfo(Long userId, ProfileChangesDto profileChangesDto) {
        Optional<User> user = userRepository.findById(userId);
        String nowPassword = profileChangesDto.getNowPassword();
        String newPassword = profileChangesDto.getNewPassword();
        String userProfileIntro = profileChangesDto.getUserProfileIntro();

//        if (nowPassword.equals("")) {
//
//        } else {
//            String encodedNowPassword = passwordEncoder.encode(nowPassword);
//            if (encodedNowPassword.equals(user.get().getPassword())) {
//                String encodedNewPassword = passwordEncoder.encode(newPassword);
//                user.get().setPassword(encodedNewPassword);
//            } else {
//                return "passwordError";
//            }
//        }

        if (!userProfileIntro.equals(user.get().getUserProfileIntro())) {
            user.get().setUserProfileIntro(userProfileIntro);
        }
        return "success";
    }

    @Transactional
    public void resetUserProfileImage(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        user.get().setUserProfileImageUrl("");
    }
}
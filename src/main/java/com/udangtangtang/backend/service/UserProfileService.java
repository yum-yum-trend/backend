package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.FileFolder;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.dto.ProfileRequestDto;
import com.udangtangtang.backend.repository.ArticleRepository;
import com.udangtangtang.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
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
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NullPointerException("해당 유저가 존재하지 않습니다!"));
        String url = fileProcessService.uploadImage(newProfileImage, FileFolder.PROFILE_IMAGES);
        user.setUserProfileImageUrl(url);
        return user.getUserProfileImageUrl();
    }

    public String getUserProfileImageUrl(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NullPointerException("해당 유저가 존재하지 않습니다!"));
        return user.getUserProfileImageUrl();
    }

    @Transactional
    public void updateUserProfileInfo(Long userId, ProfileRequestDto profileRequestDto) throws Exception  {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NullPointerException("해당 유저가 존재하지 않습니다!"));
        String nowPassword = profileRequestDto.getNowPassword();
        String newPassword = profileRequestDto.getNewPassword();
        String userProfileIntro = profileRequestDto.getUserProfileIntro();

        if (!nowPassword.isEmpty()) {
            authenticate(user.getUsername(), nowPassword);
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedNewPassword);
        }

        if (!userProfileIntro.equals(user.getUserProfileIntro())) {
            user.setUserProfileIntro(userProfileIntro);
        }
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    @Transactional
    public void resetUserProfileImage(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NullPointerException("해당 유저가 존재하지 않습니다!"));
        user.setUserProfileImageUrl("");
    }
}
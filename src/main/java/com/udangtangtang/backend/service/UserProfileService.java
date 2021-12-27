package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.*;
import com.udangtangtang.backend.dto.request.ProfileRequestDto;
import com.udangtangtang.backend.exception.ApiRequestException;
import com.udangtangtang.backend.repository.ArticleRepository;
import com.udangtangtang.backend.repository.LikesRepository;
import com.udangtangtang.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final ArticleRepository articleRepository;
    private final LikesRepository likesRepository;

    private final FileProcessService fileProcessService;


    public Optional<User> getUserProfileInfo(Long userId) {
        return userRepository.findById(userId);
    }

    public Page<Article> getUserArticles(Long userId, String sortBy, boolean isAsc, int page) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, 32, sort);

        return articleRepository.findAllByUserId(userId, pageable);
    }

    public List<Object> getUserBookmarks(Long userId, String sortBy, boolean isAsc, int page) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, 32, sort);

        Page<Likes> articlesLiked = likesRepository.findAllByUserId(userId, pageable);
        List<Article> articles = new ArrayList<>();
        List<Object> data = new ArrayList<>();

        for (Likes likes : articlesLiked) {
            Article article = articleRepository.findById(likes.getArticleId()).orElseThrow(
                    () -> new ApiRequestException("해당 게시글이 없습니다."));
            articles.add(article);
        }

        data.add(articlesLiked);
        data.add(articles);
        return data;
    }

    @Transactional
    public String updateProfileImage(Long userId, MultipartFile newProfileImage) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApiRequestException("해당 유저가 존재하지 않습니다!"));

        deleteUserProfileImageFromS3(user);

        String fileName = fileProcessService.createFileName(FileFolder.PROFILE_IMAGES, newProfileImage.getOriginalFilename());
        String url = fileProcessService.uploadImage(newProfileImage, fileName);
        user.setUserProfileImageUrl(url);
        user.setUserProfileImageName(fileName);

        return user.getUserProfileImageUrl();
    }

    public String getUserProfileImageUrl(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApiRequestException("해당 유저가 존재하지 않습니다!"));
        return user.getUserProfileImageUrl();
    }

    @Transactional
    public void updateUserPassword(Long userId, ProfileRequestDto profileRequestDto) throws Exception {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApiRequestException("해당 유저가 존재하지 않습니다!"));
        String nowPassword = profileRequestDto.getNowPassword();
        String newPassword = profileRequestDto.getNewPassword();

        if (!nowPassword.isEmpty()) {
            authenticate(user.getUsername(), nowPassword);
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedNewPassword);
        }
    }

    @Transactional
    public String updateUserProfileIntroText(Long userId, ProfileRequestDto profileRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApiRequestException("해당 유저가 존재하지 않습니다!"));
        String userProfileIntro = profileRequestDto.getUserProfileIntro();
        if (!userProfileIntro.equals(user.getUserProfileIntro())) {
            user.setUserProfileIntro(userProfileIntro);
        }
        return user.getUserProfileIntro();
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
                () -> new ApiRequestException("해당 유저가 존재하지 않습니다!"));

        deleteUserProfileImageFromS3(user);

        user.setUserProfileImageUrl(null);
    }

    // 기존에 업로드된 사용지 프로필 이미지 삭제
    private void deleteUserProfileImageFromS3(User user) {
        if(user.getUserProfileImageUrl() != null) {
            fileProcessService.deleteImage(user.getUserProfileImageName());
        }
    }

}

package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.repository.ArticleRepository;
import com.udangtangtang.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final FileService fileService;
    private final ArticleRepository articleRepository;
    // private final CommentRepository commentRepository;

    public Optional<User> getUserProfileInfo(Long userId) {
        return userRepository.findById(userId);
    }

    public List<Article> getUserArticles(Long userId) {
        return articleRepository.findAllByUserId(userId);
    }

    @Transactional
    public String updateProfileImage(Long userId, MultipartFile newProfileImage) {
        Optional<User> user = userRepository.findById(userId);
        String url = fileService.uploadImage(newProfileImage);
        user.get().updateUserProfileImageUrl(url);
        return user.get().getUserProfileImageUrl();
    }



//    public List<Comment> getUserComments(Long id) {
//        return commentRepository.findAllById(id);
//    }

}

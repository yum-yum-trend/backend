package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.JwtRefreshToken;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.domain.UserRole;
import com.udangtangtang.backend.dto.request.*;
import com.udangtangtang.backend.dto.response.JwtTokenResponseDto;
import com.udangtangtang.backend.dto.response.LoginResponseDto;
import com.udangtangtang.backend.exception.ApiRequestException;
import com.udangtangtang.backend.repository.JwtRefreshTokenRepository;
import com.udangtangtang.backend.repository.UserRepository;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.security.kakao.KakaoOAuth2;
import com.udangtangtang.backend.security.kakao.KakaoUserInfo;
import com.udangtangtang.backend.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtRefreshTokenRepository jwtRefreshTokenRepository;
    private final KakaoOAuth2 kakaoOAuth2;
    private static final String ADMIN_TOKEN = "AAABnv/xRVklrnYxKZ0aHgTBcXukeZygoC";

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService, JwtTokenUtil jwtTokenUtil,
                       JwtRefreshTokenRepository jwtRefreshTokenRepository, KakaoOAuth2 kakaoOAuth2) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtRefreshTokenRepository = jwtRefreshTokenRepository;
        this.kakaoOAuth2 = kakaoOAuth2;
    }

    @Transactional
    public User createUser(SignupRequestDto signupRequestDto) throws ApiRequestException {
        String username = signupRequestDto.getUsername();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());
        String email = signupRequestDto.getEmail();
        UserRole role = UserRole.USER;

        User user = new User(username, password, email, role);
        return userRepository.save(user);
    }

    public void checkUsername(SignupUsernameRequestDto signupUsernameRequestDto) throws ApiRequestException {
        String username = signupUsernameRequestDto.getUsername();
        // ??????????????? ?????? ??????
        Optional<User> found = userRepository.findByUsername(username);
        if (found.isPresent()) {
            throw new ApiRequestException("????????? ????????? ????????? ???????????????.");
        }
    }

    public ResponseEntity<?> createAuthenticationToken(UserRequestDto userRequestDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userRequestDto.getUsername(), userRequestDto.getPassword()));

        final String accessToken = jwtTokenUtil.generateAccessToken(userRequestDto.getUsername());
        final String refreshToken = jwtTokenUtil.generateRefreshToken();

        // FIXME] ?????? ????????? ???????????? username ??? ?????? userId
        // ????????????????????? Refresh Token ??????
        jwtRefreshTokenRepository.save(new JwtRefreshToken(userRequestDto.getUsername(), refreshToken));

        final UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(userRequestDto.getUsername());

        return ResponseEntity.ok(new LoginResponseDto(userDetails.getId(), userRequestDto.getUsername(), accessToken, refreshToken));
    }

    public ResponseEntity<?> createAuthenticationTokenByKakao(SocialLoginRequestDto socialLoginRequestDto) {
        /* ????????? ?????? ????????????????????? ?????? */
        KakaoUserInfo userInfo = kakaoOAuth2.getUserInfo(socialLoginRequestDto.getToken());
        Long kakaoId = userInfo.getId();
        String nickname = userInfo.getNickname();
        String email = userInfo.getEmail();

        String username = nickname;
        String password = kakaoId + ADMIN_TOKEN;

        // ????????? ????????? ?????? ??????
        User kakaoUser = userRepository.findByKakaoId(kakaoId)
                .orElse(null);

        // ????????? ?????? ??????
        if (kakaoUser == null) {
            String encodedPassword = passwordEncoder.encode(password);
            UserRole role = UserRole.USER;

            kakaoUser = new User(nickname, encodedPassword, email, role, kakaoId);
            userRepository.save(kakaoUser);
        }

        /* ????????? ?????? */
        Authentication kakaoUsernamePassword = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(kakaoUsernamePassword);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        /* ?????? ?????? */
        final String accessToken = jwtTokenUtil.generateAccessToken(username);
        final String refreshToken = jwtTokenUtil.generateRefreshToken();

        // ????????????????????? Refresh Token ??????
        jwtRefreshTokenRepository.save(new JwtRefreshToken(username, refreshToken));

        final UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

        return ResponseEntity.ok(new LoginResponseDto(userDetails.getId(), username, accessToken, refreshToken));
    }

    public ResponseEntity<?> reissueAuthenticationToken(TokenRequestDto tokenRequestDto) {
        // ?????????????????? ?????? Refresh Token ????????? ??????
        // Refresh Token ?????? ???????????? ?????? ?????????
        if (jwtTokenUtil.isTokenExpired(tokenRequestDto.getRefreshToken()) || !jwtTokenUtil.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new IllegalArgumentException("????????? ???????????????. ?????? ?????????????????????.");
        }

        // Access Token ??? ????????? ????????? ?????? ????????????
        String username = jwtTokenUtil.getUsernameFromToken(tokenRequestDto.getAccessToken());

        // ????????????????????? ????????? Refresh Token ??? ??????
        JwtRefreshToken jwtRefreshToken = jwtRefreshTokenRepository.findById(username).orElseThrow(
                () -> new ApiRequestException("????????? ???????????????. ?????? ?????????????????????.")
        );
        if (!jwtRefreshToken.getRefreshToken().equals(tokenRequestDto.getRefreshToken())) {
            throw new IllegalArgumentException("Refresh Token ????????? ???????????? ????????????.");
        }

        // ????????? Access Token ??????
        final String accessToken = jwtTokenUtil.generateAccessToken(username);

        return ResponseEntity.ok(new JwtTokenResponseDto(accessToken));
    }

    public void deleteAuthenticationToken(LogoutRequestDto logoutRequestDto) {
        jwtRefreshTokenRepository.deleteById(logoutRequestDto.getUsername());
    }
}
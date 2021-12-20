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
        // 사용자이름 중복 확인
        Optional<User> found = userRepository.findByUsername(username);
        if (found.isPresent()) {
            throw new ApiRequestException("중복된 사용자 이름이 존재합니다.");
        }
    }

    public ResponseEntity<?> createAuthenticationToken(UserRequestDto userRequestDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userRequestDto.getUsername(), userRequestDto.getPassword()));

        final String accessToken = jwtTokenUtil.generateAccessToken(userRequestDto.getUsername());
        final String refreshToken = jwtTokenUtil.generateRefreshToken();

        // FIXME] 현재 사용자 식별자는 username 이 아닌 userId
        // 데이터베이스에 Refresh Token 저장
        jwtRefreshTokenRepository.save(new JwtRefreshToken(userRequestDto.getUsername(), refreshToken));

        final UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(userRequestDto.getUsername());

        return ResponseEntity.ok(new LoginResponseDto(userDetails.getId(), userRequestDto.getUsername(), accessToken, refreshToken));
    }

    public ResponseEntity<?> createAuthenticationTokenByKakao(SocialLoginRequestDto socialLoginRequestDto) {
        /* 카카오 정보 데이터베이스에 저장 */
        KakaoUserInfo userInfo = kakaoOAuth2.getUserInfo(socialLoginRequestDto.getToken());
        Long kakaoId = userInfo.getId();
        String nickname = userInfo.getNickname();
        String email = userInfo.getEmail();

        String username = nickname;
        String password = kakaoId + ADMIN_TOKEN;

        // 카카오 아이디 중복 확인
        User kakaoUser = userRepository.findByKakaoId(kakaoId)
                .orElse(null);

        // 카카오 정보 저장
        if (kakaoUser == null) {
            String encodedPassword = passwordEncoder.encode(password);
            UserRole role = UserRole.USER;

            kakaoUser = new User(nickname, encodedPassword, email, role, kakaoId);
            userRepository.save(kakaoUser);
        }

        /* 로그인 처리 */
        Authentication kakaoUsernamePassword = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(kakaoUsernamePassword);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        /* 토큰 처리 */
        final String accessToken = jwtTokenUtil.generateAccessToken(username);
        final String refreshToken = jwtTokenUtil.generateRefreshToken();

        // 데이터베이스에 Refresh Token 저장
        jwtRefreshTokenRepository.save(new JwtRefreshToken(username, refreshToken));

        final UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

        return ResponseEntity.ok(new LoginResponseDto(userDetails.getId(), username, accessToken, refreshToken));
    }

    public ResponseEntity<?> reissueAuthenticationToken(TokenRequestDto tokenRequestDto) {
        // 사용자로부터 받은 Refresh Token 유효성 검사
        // Refresh Token 마저 만료되면 다시 로그인
        if (jwtTokenUtil.isTokenExpired(tokenRequestDto.getRefreshToken()) || !jwtTokenUtil.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new IllegalArgumentException("잘못된 요청입니다. 다시 로그인해주세요.");
        }

        // Access Token 에 기술된 사용자 이름 가져오기
        String username = jwtTokenUtil.getUsernameFromToken(tokenRequestDto.getAccessToken());

        // 데이터베이스에 저장된 Refresh Token 과 비교
        JwtRefreshToken jwtRefreshToken = jwtRefreshTokenRepository.findById(username).orElseThrow(
                () -> new ApiRequestException("잘못된 요청입니다. 다시 로그인해주세요.")
        );
        if (!jwtRefreshToken.getRefreshToken().equals(tokenRequestDto.getRefreshToken())) {
            throw new IllegalArgumentException("Refresh Token 정보가 일치하지 않습니다.");
        }

        // 새로운 Access Token 발급
        final String accessToken = jwtTokenUtil.generateAccessToken(username);

        return ResponseEntity.ok(new JwtTokenResponseDto(accessToken));
    }

    public void deleteAuthenticationToken(LogoutRequestDto logoutRequestDto) {
        jwtRefreshTokenRepository.deleteById(logoutRequestDto.getUsername());
    }
}
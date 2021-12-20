package com.udangtangtang.backend.controller;

import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.dto.request.*;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.UserService;
import com.udangtangtang.backend.util.JwtTokenUtil;

import com.udangtangtang.backend.validation.ValidationSequence;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/signup")
    public User createUser(@Validated(ValidationSequence.class) @RequestBody SignupRequestDto signupRequestDto) {
        return userService.createUser(signupRequestDto);
    }

    @PostMapping(value = "/signup/username")
    public void checkUsername(@Validated(ValidationSequence.class) @RequestBody SignupUsernameRequestDto signupUsernameRequestDto) {
        userService.checkUsername(signupUsernameRequestDto);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody UserRequestDto userRequestDto) {
        return userService.createAuthenticationToken(userRequestDto);
    }

    @PostMapping(value = "/login/kakao")
    public ResponseEntity<?> createAuthenticationTokenByKakao(@RequestBody SocialLoginRequestDto socialLoginRequestDto) {
        return userService.createAuthenticationTokenByKakao(socialLoginRequestDto);
    }

    @PostMapping(value = "/auth/token")
    public ResponseEntity<?> reissueAuthenticationToken(@RequestBody TokenRequestDto tokenRequestDto) {
        return userService.reissueAuthenticationToken(tokenRequestDto);
    }

    @PostMapping(value = "/logout")
    public void deleteAuthenticationToken(@RequestBody LogoutRequestDto logoutRequestDto) {
        userService.deleteAuthenticationToken(logoutRequestDto);
    }
}
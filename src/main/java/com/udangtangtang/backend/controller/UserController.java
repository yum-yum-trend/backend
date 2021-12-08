package com.udangtangtang.backend.controller;

import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.dto.request.SignupRequestDto;
import com.udangtangtang.backend.dto.request.SocialLoginRequestDto;
import com.udangtangtang.backend.dto.request.TokenRequestDto;
import com.udangtangtang.backend.dto.request.UserRequestDto;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.UserService;
import com.udangtangtang.backend.util.JwtTokenUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    @PostMapping(value = "/signup")
    public User createUser(@RequestBody SignupRequestDto signupRequestDto) {
        return userService.createUser(signupRequestDto);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody UserRequestDto userRequestDto) {
//        authenticate(userRequestDto.getUsername(), userRequestDto.getPassword());
//        final UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(userRequestDto.getUsername());
//        final String token = jwtTokenUtil.generateToken(userDetails);
//        return ResponseEntity.ok(new JwtResponse(token, userDetails.getId(), userDetails.getUsername()));

        return userService.createAuthenticationToken(userRequestDto);
    }

    @PostMapping(value = "/login/kakao")
    public ResponseEntity<?> createAuthenticationTokenByKakao(@RequestBody SocialLoginRequestDto socialLoginRequestDto) {
//        String username = userService.kakaoLogin(socialLoginRequestDto.getToken());
//        final UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
//        final String token = jwtTokenUtil.generateToken(userDetails);
//        return ResponseEntity.ok(new JwtResponse(token, userDetails.getId(), userDetails.getUsername()));

        return userService.createAuthenticationTokenByKakao(socialLoginRequestDto);
    }

    @PostMapping(value = "/auth/token")
    public ResponseEntity<?> reissueAuthenticationToken(@RequestBody TokenRequestDto tokenRequestDto) {
        return userService.reissueAuthenticationToken(tokenRequestDto);
    }

//    private void authenticate(String username, String password) throws Exception {
//        try {
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
//        } catch (DisabledException e) {
//            throw new Exception("USER_DISABLED", e);
//        } catch (BadCredentialsException e) {
//            throw new Exception("INVALID_CREDENTIALS", e);
//        }
//    }
}
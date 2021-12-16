package com.udangtangtang.backend.filter;

import com.udangtangtang.backend.util.JwtTokenUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter  extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    String HEADER_STRING = "Authorization";
    String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(HEADER_STRING);
        String accessToken = (header != null && header.startsWith(TOKEN_PREFIX)) ? header.replace(TOKEN_PREFIX,"") : null;

        // Request Header 에 Access Token (Authorization) 이 담긴 경우
        if (!ObjectUtils.isEmpty(accessToken)) {
            // Access Token 이 만료된 경우
            if(jwtTokenUtil.isTokenExpired(accessToken)) {
                throw new JwtException("access token is expired");
            }

            // Access Token 이 유효한 경우
            if(jwtTokenUtil.validateToken(accessToken) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtTokenUtil.getUsernameFromToken(accessToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                logger.info("authenticated user " + username + ", setting security context");

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {
            logger.warn("couldn't find bearer string, will ignore the header");
        }
        chain.doFilter(req, res);
    }
}



//if (username != null && SecurityContextHolder.getContext().getAuthentication() == null)

package com.example.ckdatveexe.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        log.info("🔍 [AUTH FILTER] Processing request: {} {}", request.getMethod(), requestPath);

        try {
            String jwt = parseJwt(request);

            if (jwt != null) {
                log.info("✅ [AUTH FILTER] JWT token found in request");

                if (jwtUtils.validateJwtToken(jwt)) {
                    log.info("✅ [AUTH FILTER] JWT token is valid");

                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    log.info("✅ [AUTH FILTER] Username from token: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    log.info("✅ [AUTH FILTER] User loaded with authorities: {}", userDetails.getAuthorities());

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("✅ [AUTH FILTER] Authentication set successfully for user: {}", username);
                } else {
                    log.warn("❌ [AUTH FILTER] JWT token is invalid");
                }
            } else {
                log.warn("❌ [AUTH FILTER] No JWT token found in request");
            }
        } catch (Exception e) {
            log.error("❌ [AUTH FILTER] Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        // First try to get token from cookie
        String jwt = getJwtFromCookie(request);
        if (jwt != null) {
            log.info("🍪 [AUTH FILTER] JWT token found in cookie");
            return jwt;
        }

        // Fallback to Authorization header
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            log.info("🔑 [AUTH FILTER] JWT token found in Authorization header");
            return headerAuth.substring(7);
        }

        log.info("🚫 [AUTH FILTER] No JWT token found in cookie or Authorization header");
        return null;
    }

    private String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
package com.example.ckdatveexe.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class DebugSecurityFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                        FilterChain filterChain) throws ServletException, IOException {

                String path = request.getRequestURI();
                String method = request.getMethod();

                log.info("🔍 [DEBUG SECURITY] Before filter chain - {} {}", method, path);

                // Safe authentication logging to avoid LazyInitializationException
                var auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) {
                        log.info("🔍 [DEBUG SECURITY] Authentication: {} ({})",
                                        auth.getClass().getSimpleName(),
                                        auth.getName() != null ? auth.getName() : "anonymous");
                } else {
                        log.info("🔍 [DEBUG SECURITY] Authentication: null");
                }

                filterChain.doFilter(request, response);

                log.info("🔍 [DEBUG SECURITY] After filter chain - {} {} - Status: {}",
                                method, path, response.getStatus());

                // Safe final authentication logging
                var finalAuth = SecurityContextHolder.getContext().getAuthentication();
                if (finalAuth != null) {
                        log.info("🔍 [DEBUG SECURITY] Final Authentication: {} ({})",
                                        finalAuth.getClass().getSimpleName(),
                                        finalAuth.getName() != null ? finalAuth.getName() : "anonymous");
                } else {
                        log.info("🔍 [DEBUG SECURITY] Final Authentication: null");
                }
        }
}
package com.longvo.demo_identity_service.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RefreshTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Chỉ xử lý cho /auth/refresh
        if (request.getRequestURI().equals("/identity/auth/refresh")) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refreshToken".equals(cookie.getName())) {
                        String refreshToken = cookie.getValue();

                        // Gán vào attribute để Controller hoặc Service dùng
                        request.setAttribute("refreshToken", refreshToken);
                        break;
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}


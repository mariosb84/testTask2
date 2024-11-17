package com.example.itemservice.filter;

import com.example.itemservice.service.JwtServiceData;
import com.example.itemservice.service.TokenBlackListServiceData;
import com.example.itemservice.service.UserServiceData;
import liquibase.util.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final JwtServiceData jwtServiceData;
    private final UserServiceData userService;
    private final TokenBlackListServiceData tokenBlackListServiceData;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        /* Получаем токен из заголовка*/
        var authHeader = request.getHeader(HEADER_NAME);
        if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        /* Обрезаем префикс и получаем имя пользователя из токена*/
        var jwt = authHeader.substring(BEARER_PREFIX.length());
        var username = jwtServiceData.extractUserName(jwt);

        if (StringUtils.isNotEmpty(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService
                    .userDetailsService()
                    .loadUserByUsername(username);

            /* Если токен валиден, то аутентифицируем пользователя*/
            if (
                    (jwtServiceData.isTokenValid(jwt, userDetails))
                            && (
                            (tokenBlackListServiceData.findByToken(jwt).isEmpty())
                                    || !(tokenBlackListServiceData.findByToken(jwt).get().getToken().equals(jwt))
                    )
            ) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        }
        filterChain.doFilter(request, response);
    }

}

package com.lottery.api.infrastructure.config.security;

import com.lottery.api.domain.model.UserActivityLog;
import com.lottery.api.domain.port.out.UserActivityRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityLoggingFilter extends OncePerRequestFilter {

    private static final Pattern LOTTERY_TYPE_PATTERN =
            Pattern.compile("/(MELATE|REVANCHA|REVANCHITA|GANA_GATO)/", Pattern.CASE_INSENSITIVE);

    private final UserActivityRepositoryPort activityRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        filterChain.doFilter(request, response);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserDetails ud)) {
            return;
        }

        String userId = ud.getUsername();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String action = deriveAction(method, uri);
        String lotteryType = extractLotteryType(uri);

        try {
            activityRepository.save(UserActivityLog.builder()
                    .userId(userId)
                    .endpoint(uri)
                    .httpMethod(method)
                    .action(action)
                    .lotteryType(lotteryType)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("No se pudo registrar actividad de usuario {}: {}", userId, e.getMessage());
        }
    }

    private String deriveAction(String method, String uri) {
        if (uri.contains("/predictions") && uri.contains("/analyze")) return "ANALYZE_PREDICTION";
        if (uri.contains("/predictions") && "POST".equals(method)) return "SAVE_PREDICTION";
        if (uri.contains("/predictions") && "GET".equals(method)) return "LIST_PREDICTIONS";
        if (uri.contains("/predictions") && "DELETE".equals(method)) return "DELETE_PREDICTION";
        if (uri.contains("/sync")) return "SYNC_DATA";
        if (uri.contains("/statistics")) return "VIEW_STATISTICS";
        if (uri.contains("/predictions/")) return "VIEW_DRAW_RESULTS";
        return method + ":" + uri;
    }

    private String extractLotteryType(String uri) {
        Matcher m = LOTTERY_TYPE_PATTERN.matcher(uri);
        return m.find() ? m.group(1).toUpperCase() : null;
    }
}

package com.homihq.db2rest.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homihq.db2rest.auth.common.AbstractAuthProvider;
import com.homihq.db2rest.auth.common.UserDetail;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
public class AuthFilter extends OncePerRequestFilter {

    private final AbstractAuthProvider authProvider;
    private final ObjectMapper objectMapper;
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {

        log.debug("Handling Auth");

        String requestUri = urlPathHelper.getRequestUri(request);
        String method = request.getMethod();

        log.debug("Request URI - {}", requestUri);

        if (!authProvider.isExcluded(requestUri, method)) {

            //authenticate
            UserDetail userDetail = authProvider.authenticate(request);

            log.debug("user detail - {}", userDetail);

            if (Objects.isNull(userDetail)) {
                String errorMessage = "Authentication failure.";
                addError(errorMessage, request, response);
                return;
            }

            //authorize
            boolean authorized =
                    authProvider.authorize(userDetail, requestUri, method);

            if (!authorized) {
                String errorMessage = "Authorization failure.";
                addError(errorMessage, request, response);
                return;
            }
        } else {
            log.debug("URI in whitelist. Security checks not applied.");
        }

        filterChain.doFilter(request, response);

        logger.debug("Completed Auth Filter");
    }

    private void addError(
            String errorMessage,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        var body = new LinkedHashMap<>();
        body.put("type", "https://db2rest/unauthorized");
        body.put("title", "Auth Error");
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        body.put("detail", errorMessage);
        body.put("instance", request.getRequestURI());
        body.put("errorCategory", "Invalid-Auth");
        body.put("timestamp", Instant.now());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getWriter(), body);
    }

}

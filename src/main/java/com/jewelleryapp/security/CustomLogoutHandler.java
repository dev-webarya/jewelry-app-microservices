package com.jewelleryapp.security;

import com.jewelleryapp.model.TokenDenyList;
import com.jewelleryapp.repository.TokenDenyListRepository;
import com.jewelleryapp.service.RefreshTokenService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomLogoutHandler implements LogoutHandler {

    private final JwtService jwtService;
    private final TokenDenyListRepository tokenDenyListRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.info("--- CustomLogoutHandler invoked ---");
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("LOGOUT FAILED: Request does not contain a Bearer token.");
            return;
        }

        jwt = authHeader.substring(7);

        try {
            final String jti = jwtService.extractJti(jwt);
            log.info("LOGOUT: Extracted JTI from token: {}", jti);

            if (jti != null && !tokenDenyListRepository.existsByJti(jti)) {
                log.info("LOGOUT: JTI is not in the deny list. Proceeding to invalidate.");
                final Date expiryDate = jwtService.extractExpiration(jwt);
                TokenDenyList deniedToken = TokenDenyList.builder()
                        .jti(jti)
                        .expiryDate(expiryDate.toInstant())
                        .build();
                tokenDenyListRepository.save(deniedToken);
                log.info("LOGOUT: Successfully saved JTI {} to the deny list.", jti);

                final String username = (authentication != null) ? authentication.getName() : jwtService.extractUsername(jwt);
                if (username != null) {
                    refreshTokenService.deleteByUsername(username);
                    log.info("LOGOUT: Refresh token for user '{}' has been deleted.", username);
                }
            } else {
                log.warn("LOGOUT: Attempted logout with a token that is already invalidated or has no JTI. JTI: {}", jti);
            }

        } catch (JwtException e) {
            log.error("LOGOUT ERROR: Error parsing JWT during logout: {}", e.getMessage());
        }
    }
}
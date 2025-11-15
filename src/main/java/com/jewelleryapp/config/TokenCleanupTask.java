package com.jewelleryapp.config;

import com.jewelleryapp.repository.TokenDenyListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling // Ensure scheduling is enabled
public class TokenCleanupTask {

    private final TokenDenyListRepository tokenDenyListRepository;

    // This cron expression means "run at midnight every day"
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void purgeExpiredTokens() {
        log.info("Running scheduled task to purge expired denied tokens.");
        tokenDenyListRepository.deleteByExpiryDateBefore(Instant.now());
        log.info("Expired denied tokens purged successfully.");
    }
}
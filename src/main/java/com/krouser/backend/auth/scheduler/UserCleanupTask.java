package com.krouser.backend.auth.scheduler;

import com.krouser.backend.auth.repository.VerificationTokenRepository;
import com.krouser.backend.users.entity.User;
import com.krouser.backend.users.entity.UserStatus;
import com.krouser.backend.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(UserCleanupTask.class);

    @Value("${app.auth.verification-expiration-hours:48}")
    private int expirationHours;

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;

    public UserCleanupTask(UserRepository userRepository, VerificationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(fixedRate = 43200000) // Run every 12 hours (12 * 60 * 60 * 1000)
    @Transactional
    public void cleanupUnverifiedUsers() {
        LocalDateTime expirationTime = LocalDateTime.now().minusHours(expirationHours);
        logger.info("Starting cleanup of unverified users created before {}", expirationTime);

        List<User> usersToDelete = userRepository.findAll().stream()
                .filter(u -> u.getStatus() == UserStatus.PENDING_VERIFICATION)
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isBefore(expirationTime))
                .collect(Collectors.toList());

        if (usersToDelete.isEmpty()) {
            logger.info("No unverified users found to delete.");
            return;
        }

        for (User user : usersToDelete) {
            // Delete associated token first
            tokenRepository.deleteByUser(user);
            // Delete user
            userRepository.delete(user);
        }

        logger.info("Purged {} unverified users.", usersToDelete.size());
    }
}

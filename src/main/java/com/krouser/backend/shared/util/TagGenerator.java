package com.krouser.backend.shared.util;

import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class TagGenerator {

    public String generateTag(String alias, UUID userId) {
        if (alias == null)
            alias = "User";
        String safeAlias = alias.trim();
        // Suffix from UUID (hex)
        String uuidHex = userId.toString().replace("-", "");
        // Use first 6 chars
        String suffix = uuidHex.substring(0, 6);
        return safeAlias + "#" + suffix;
    }

    public String generateTagWithAlternateSuffix(String alias, UUID userId, int attempt) {
        if (alias == null)
            alias = "User";
        String safeAlias = alias.trim();
        String uuidHex = userId.toString().replace("-", "");

        // If attempt 1, use different slice of UUID
        if (attempt == 1) {
            return safeAlias + "#" + uuidHex.substring(6, 12);
        }

        // Otherwise random
        int randomNum = ThreadLocalRandom.current().nextInt(100000, 999999);
        return safeAlias + "#" + randomNum;
    }
}

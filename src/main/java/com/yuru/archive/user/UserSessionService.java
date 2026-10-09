package com.yuru.archive.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final SessionRegistry sessionRegistry;

    public int expireOtherSessions(String username, String currentSessionId) {
        int expiredCount = 0;

        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (!username.equals(usernameOf(principal))) {
                continue;
            }

            for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
                if (currentSessionId != null && currentSessionId.equals(session.getSessionId())) {
                    continue;
                }
                session.expireNow();
                expiredCount++;
            }
        }

        return expiredCount;
    }

    private String usernameOf(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return String.valueOf(principal);
    }
}

package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.dto.AccountUserSecurityDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SecurityService {

    private AccountUserSecurityDetails getCurrentUser(){
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal == null){
            var errMsg = "Security principle of invalid type is null";
            throw new IllegalStateException(errMsg);
        }
        if (!(principal instanceof AccountUserSecurityDetails)){
            var errMsg = String.format("Security principle of invalid type %s",
                    principal.getClass());
            throw new IllegalStateException(errMsg);
        }

        return (AccountUserSecurityDetails) principal;
    }

    public UUID getCurrentUserId(){
        return getCurrentUser().getId();
    }

    public String getCurrentUserEmail(){
        return getCurrentUser().getUsername();
    }
}

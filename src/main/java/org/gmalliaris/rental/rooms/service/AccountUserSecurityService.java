package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.dto.AccountUserSecurityDetails;
import org.gmalliaris.rental.rooms.repository.AccountUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountUserSecurityService implements UserDetailsService {

    private final AccountUserRepository accountUserRepository;

    public AccountUserSecurityService(AccountUserRepository accountUserRepository) {
        this.accountUserRepository = accountUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        var optionalUser = accountUserRepository.findByEmail(username);
        if (optionalUser.isEmpty()){
            return null;
        }
        return new AccountUserSecurityDetails(optionalUser.get());
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID userId) throws UsernameNotFoundException {

        var optionalUser = accountUserRepository.findById(userId);
        if (optionalUser.isEmpty()){
            return null;
        }
        return new AccountUserSecurityDetails(optionalUser.get());
    }
}

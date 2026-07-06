package com.villamanager.service;

import com.villamanager.entity.User;
import com.villamanager.exception.AuthenticationFailedException;
import com.villamanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AuthenticationFailedException("Authentication is required");
        }
        Long userId = Long.valueOf(String.valueOf(authentication.getPrincipal()));
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationFailedException("Authenticated user was not found"));
    }
}

package com.atomicpay.auth.security;

import com.atomicpay.auth.domain.UserStatus;
import com.atomicpay.auth.entity.UserEntity;
import com.atomicpay.auth.exception.AuthException;
import com.atomicpay.auth.exception.ErrorCode;
import com.atomicpay.auth.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new AuthException(ErrorCode.USER_SUSPENDED);
        }
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AuthException(ErrorCode.USER_LOCKED);
        }

        return new User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}

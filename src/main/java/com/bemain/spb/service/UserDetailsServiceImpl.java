package com.bemain.spb.service;

import com.bemain.spb.entity.User;
import com.bemain.spb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // UserDetails 객체 생성 및 반환
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name()) // Enum Role을 권한으로 변환
                .accountExpired(false)      // 계정 만료 안됨
                .accountLocked(false)       // 계정 잠기지 않음
                .credentialsExpired(false)  // 비번 만료 안됨
                .disabled(false)            // 계정 활성화 됨 (disabled가 false여야 활성)
                .build();
    }
}
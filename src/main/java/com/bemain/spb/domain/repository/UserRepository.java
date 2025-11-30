package com.bemain.spb.domain.repository;

import com.bemain.spb.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 로그인할 때 아이디로 회원을 찾아야 하므로 이 메서드가 필수입니다.
    Optional<User> findByUsername(String username);

    // (선택) 중복 가입 방지용
    boolean existsByUsername(String username);
}
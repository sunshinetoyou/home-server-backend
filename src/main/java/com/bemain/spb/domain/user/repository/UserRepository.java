package com.bemain.spb.domain.user.repository;

import com.bemain.spb.domain.user.entity.Role;
import com.bemain.spb.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 로그인할 때 아이디로 회원을 찾아야 하므로 이 메서드가 필수입니다.
    Optional<User> findByUsername(String username);

    // (선택) 중복 가입 방지용
    boolean existsByUsername(String username);

    // 활성화된 랩 수 대체재 (나중에 변경 필요)
    long countByRole(Role role);
}
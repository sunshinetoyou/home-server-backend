package com.bemain.spb.domain.comment.repository;

import com.bemain.spb.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 리포트의 댓글 조회 (오래된 순)
    List<Comment> findAllByReportIdOrderByCreatedAtAsc(Long reportId);
}
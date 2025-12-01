package com.bemain.spb.domain.comment.dto;

import com.bemain.spb.domain.comment.entity.Comment;
import com.bemain.spb.domain.user.entity.RoleType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {
    private Long id;
    private String content;

    // 작성자 정보
    private String authorName; // 닉네임
    private RoleType authorRole; // 개발자인지 해커인지 구분용

    private LocalDateTime createdAt;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.authorName = comment.getAuthor().getNickname();
        this.authorRole = comment.getAuthor().getRole();
        this.createdAt = comment.getCreatedAt();
    }
}
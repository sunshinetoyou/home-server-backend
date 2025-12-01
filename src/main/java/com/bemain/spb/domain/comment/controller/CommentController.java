package com.bemain.spb.domain.comment.controller;

import com.bemain.spb.domain.comment.dto.CommentCreateRequest;
import com.bemain.spb.domain.comment.dto.CommentResponse;
import com.bemain.spb.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports") // 상위 경로를 reports로 잡음
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    // POST /api/v1/reports/{reportId}/comments
    @PostMapping("/{reportId}/comments")
    public ResponseEntity<Map<String, Object>> createComment(
            @PathVariable Long reportId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CommentCreateRequest request
    ) {
        Long commentId = commentService.createComment(reportId, userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "댓글이 등록되었습니다.", "commentId", commentId));
    }

    // 댓글 목록 조회
    // GET /api/v1/reports/{reportId}/comments
    @GetMapping("/{reportId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long reportId) {
        return ResponseEntity.ok(commentService.getComments(reportId));
    }
}
package com.bemain.spb.domain.comment.service;

import com.bemain.spb.domain.comment.dto.CommentCreateRequest;
import com.bemain.spb.domain.comment.dto.CommentResponse;
import com.bemain.spb.domain.comment.entity.Comment;
import com.bemain.spb.domain.comment.repository.CommentRepository;
import com.bemain.spb.domain.report.entity.Report;
import com.bemain.spb.domain.report.repository.ReportRepository;
import com.bemain.spb.domain.user.entity.User;
import com.bemain.spb.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // 댓글 작성
    @Transactional
    public Long createComment(Long reportId, String username, CommentCreateRequest request) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트 없음"));

        Comment comment = Comment.builder()
                .report(report)
                .author(author)
                .content(request.getContent())
                .build();

        return commentRepository.save(comment).getId();
    }

    // 댓글 목록 조회
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long reportId) {
        // 리포트 존재 여부 확인 (없으면 에러)
        if (!reportRepository.existsById(reportId)) {
            throw new IllegalArgumentException("리포트가 존재하지 않습니다.");
        }

        return commentRepository.findAllByReportIdOrderByCreatedAtAsc(reportId)
                .stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }
}
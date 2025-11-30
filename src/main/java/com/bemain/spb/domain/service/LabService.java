package com.bemain.spb.domain.service;

import com.bemain.spb.domain.dto.lab.LabSummaryResponse;
import com.bemain.spb.domain.repository.LabRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabService {

    private final LabRepository labRepository;

    public List<LabSummaryResponse> getActiveLabList() {
        return labRepository.findAllActiveLabsWithStats();
    }
}
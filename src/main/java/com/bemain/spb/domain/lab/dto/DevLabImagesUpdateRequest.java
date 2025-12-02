package com.bemain.spb.domain.lab.dto;

import com.bemain.spb.domain.lab.entity.LabDbType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DevLabImagesUpdateRequest {
    // 셋 중 하나만 바뀔 수도 있으니 Nullable
    private String feImage;
    private String beImage;
    private LabDbType dbType; // CONTAINER_IMAGE or SQLITE_SCRIPT
    private String dbSource;  // "postgres:15" or "CREATE TABLE..."
}
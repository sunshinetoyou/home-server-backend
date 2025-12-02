package com.bemain.spb.domain.lab.entity;

public enum LabStatus {
    PENDING,  // 배포 중 (ContainerCreating, Pending)
    RUNNING,  // 정상 실행 중 (Running)
    ERROR,    // 에러 발생 (ErrImagePull, CrashLoopBackOff 등)
    STOPPED   // 종료됨
}
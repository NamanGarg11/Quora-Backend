package com.example.QuoraApp.dto;

import com.example.QuoraApp.enums.TargetType;

import java.time.LocalDateTime;

public class LikeResponseDTO {
    private String id;
    private String targetId;
    private TargetType targetType;
    private Boolean isLike;
    private LocalDateTime createdAt;

}

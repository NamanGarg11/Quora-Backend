package com.example.QuoraApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserResponseDTO {
    private String id;
    private String name;
    private String phoneNumber;
    private List<String> followedQuestionIds;
    private List<String> followingUserIds;
    private List<String> followerUserIds;
    private LocalDateTime createdAt;
}

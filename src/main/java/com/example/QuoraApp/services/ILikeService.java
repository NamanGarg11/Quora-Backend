package com.example.QuoraApp.services;

import com.example.QuoraApp.dto.LikeRequestDTO;
import com.example.QuoraApp.dto.LikeResponseDTO;
import com.example.QuoraApp.enums.TargetType;
import reactor.core.publisher.Mono;

public interface ILikeService {
    Mono<LikeResponseDTO> createLike(LikeRequestDTO likeRequestDTO);
    Mono<LikeResponseDTO> countLikesByTargetIdAndTargetType(String targetId, TargetType targetType);
    Mono<LikeResponseDTO> countDisLikesByTargetIdAndTargetType(String targetId, TargetType targetType);
    Mono<LikeResponseDTO> toggleLike(String targetId, TargetType targetType);


}

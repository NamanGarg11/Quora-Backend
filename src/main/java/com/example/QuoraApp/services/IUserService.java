package com.example.QuoraApp.services;

import com.example.QuoraApp.dto.CreateUserRequestDTO;
import com.example.QuoraApp.dto.CreateUserResponseDTO;
import com.example.QuoraApp.dto.QuestionResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IUserService {
    Mono<CreateUserResponseDTO> createUser(CreateUserRequestDTO createUserRequestDTO);
    Mono<CreateUserResponseDTO> followQuestion(String userId , String questionId);
    Flux<QuestionResponseDTO> getUserFeeds(String userId);
    Mono<Void> followUser(String followerId, String followingId);
//    Flux<QuestionResponseDTO> getUserFollowerFeed(String userId);

}

package com.example.QuoraApp.controllers;

import com.example.QuoraApp.dto.CreateUserRequestDTO;
import com.example.QuoraApp.dto.CreateUserResponseDTO;
import com.example.QuoraApp.dto.QuestionResponseDTO;
import com.example.QuoraApp.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final IUserService userService;

    @PostMapping()
    public Mono<CreateUserResponseDTO> createUser(@RequestBody CreateUserRequestDTO createUserRequestDTO) {
        return userService.createUser(createUserRequestDTO)
                .doOnSuccess(response -> System.out.println("User created successfully: " + response))
                .doOnError(error -> System.out.println("Error creating question: " + error));
    }

    @PostMapping("/{userId}/follow/question/{questionId}")
    public Mono<CreateUserResponseDTO> followQuestion(@PathVariable String userId, @PathVariable String questionId) {
        return userService.followQuestion(userId , questionId)
                .doOnSuccess(response -> System.out.println("FollowQuestion done successfully: " + response))
                .doOnError(error -> System.out.println("Error following question: " + error));
    }

    @GetMapping("/feeds/{userId}")
    public Flux<QuestionResponseDTO> getUserFeeds(@PathVariable String userId) {
        return userService.getUserFeeds(userId)
                .doOnError(error -> System.out.println("Error fetching feeds: " + error))
                .doOnComplete(() -> System.out.println("User Feeds fetched successfully"));
    }

    @PostMapping("/{followerId}/follow/user/{followingId}")
    public Mono<Void> followUser(@PathVariable String followerId , @PathVariable String followingId) {
        return userService.followUser(followerId , followingId)
                .doOnSuccess(response -> System.out.println("Follow User done successfully: " + response))
                .doOnError(error -> System.out.println("Error following question: " + error));
    }
}

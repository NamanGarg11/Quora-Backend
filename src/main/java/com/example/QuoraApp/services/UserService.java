package com.example.QuoraApp.services;

import com.example.QuoraApp.dto.CreateUserRequestDTO;
import com.example.QuoraApp.dto.CreateUserResponseDTO;
import com.example.QuoraApp.dto.QuestionResponseDTO;
import com.example.QuoraApp.mapper.QuestionMapper;
import com.example.QuoraApp.mapper.UserMapper;
import com.example.QuoraApp.models.Question;
import com.example.QuoraApp.models.User;
import com.example.QuoraApp.repositories.IQuestionRepository;
import com.example.QuoraApp.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.domain.Range;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final IUserRepository userRepository;
    private final IQuestionRepository questionRepository;
    private final ReactiveRedisTemplate<String, QuestionResponseDTO> redisTemplate;

    // helper methods to generate cache key
    private String getUserFeedCacheKey(String userId) {
        return "user:feed:" + userId;
    }

    private String getFollowerFeedCacheKey(String userId) {
        return "user:follower_feed:" + userId;
    }

    @Override
    public Mono<CreateUserResponseDTO> createUser(CreateUserRequestDTO createUserRequestDTO) {
        User user = User.builder()
                .name(createUserRequestDTO.getName())
                .phoneNumber(createUserRequestDTO.getPhoneNumber())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return userRepository.save(user)
                .map(UserMapper::toUserResponseDTO)
                .doOnSuccess(response -> System.out.println("User created successfully: " + response))
                .doOnError(error -> System.out.println("Error creating user: " + error));
    }

    @Override
    public Mono<CreateUserResponseDTO> followQuestion(String userId, String questionId) {
        return userRepository.findById(userId)
                .zipWith(questionRepository.findById(questionId))
                .flatMap(tuple -> {
                    User user = tuple.getT1();
                    Question question = tuple.getT2();

                    if (!user.getFollowedQuestionIds().contains(questionId)) {
                        user.getFollowedQuestionIds().add(questionId);

                        String cacheKey = getUserFeedCacheKey(userId);
                        double score = System.currentTimeMillis();
                        QuestionResponseDTO questionDTO = QuestionMapper.toQuestionResponseDTO(question);

                        return userRepository.save(user)
                                .then(redisTemplate.opsForZSet().add(cacheKey, questionDTO, score))
                                .thenReturn(user);
                    }
                    return Mono.just(user);
                })
                .map(UserMapper::toUserResponseDTO)
                .switchIfEmpty(Mono.error(new RuntimeException("User or Question not found")));
    }


    @Override
    public Flux<QuestionResponseDTO> getUserFeeds(String userId) {
        String cacheKey = getUserFeedCacheKey(userId);

        Range<Long> range = Range.from(Range.Bound.inclusive(0L)).to(Range.Bound.inclusive(-1L));
        return redisTemplate.opsForZSet().reverseRange(cacheKey, range)
                .switchIfEmpty(
                        // 2. CACHE MISS:
                        userRepository.findById(userId)
                                .flatMapMany(user -> {
                                    List<String> questionIds = user.getFollowedQuestionIds();
                                    return questionRepository.findAllById(questionIds)
                                            .map(QuestionMapper::toQuestionResponseDTO);
                                })
                                // 3. Populate the cache for next time
                                .collectList()
                                .flatMapMany(dtos -> {
                                    if (dtos.isEmpty()) {
                                        return Flux.empty();
                                    }
                                    // Populate cache with historical timestamps for correct initial sorting
                                    Mono<Long> cachePopulationMono = Flux.fromIterable(dtos)
                                            .flatMap(dto -> {
                                                double score = dto.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                                                return redisTemplate.opsForZSet().add(cacheKey, dto, score);
                                            })
                                            .count();

                                    // Set an expiration for the cache and return the feed
                                    return cachePopulationMono
                                            .then(redisTemplate.expire(cacheKey, Duration.ofHours(1)))
                                            .thenMany(Flux.fromIterable(dtos));
                                })
                );
    }


    @Override
    public Mono<Void> followUser(String followerId, String followingId) {
        // Prevent users from following themselves
        if (followerId.equals(followingId)) {
            return Mono.empty();
        }

        Mono<User> followerMono = userRepository.findById(followerId);
        Mono<User> followingMono = userRepository.findById(followingId);

        return Mono.zip(followerMono, followingMono)
                .flatMap(tuple -> {
                    User follower = tuple.getT1();
                    User following = tuple.getT2();

                    if (!follower.getFollowingUserIds().contains(followingId)) {
                        follower.getFollowingUserIds().add(followingId);
                    }
                    if (!following.getFollowerUserIds().contains(followerId)) {
                        following.getFollowerUserIds().add(followerId);
                    }

                    // When a user follows another, their follower feed is now stale.
                    // We must delete it so it can be regenerated on the next request.
                    String followerFeedCacheKey = getFollowerFeedCacheKey(followerId);

                    // Save both users, then invalidate the follower's cache
                    return userRepository.save(follower)
                            .then(userRepository.save(following))
                            .then(redisTemplate.delete(followerFeedCacheKey));

                }).then();
    }
}

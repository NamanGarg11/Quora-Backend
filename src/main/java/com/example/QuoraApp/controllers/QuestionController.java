package com.example.QuoraApp.controllers;

import com.example.QuoraApp.dto.PaginationResponseDTO;
import com.example.QuoraApp.dto.QuestionRequestDTO;
import com.example.QuoraApp.dto.QuestionResponseDTO;
import com.example.QuoraApp.services.IQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final IQuestionService questionService;
    @PostMapping()
    Mono<QuestionResponseDTO> createQuestion(@RequestBody QuestionRequestDTO questionRequestDTO) {
        return questionService.createQuestion(questionRequestDTO)
                .doOnSuccess(response -> System.out.println("Question created successfully: " + response))
                .doOnError(error -> System.out.println("Error creating question: " + error));
    }

    @GetMapping("/{id}")
    public Mono<QuestionResponseDTO> getQuestionById(@PathVariable String id) {
       return questionService.getQuestionById(id)
               .doOnSuccess(response -> System.out.println("Question fetched successfully: " + response))
               .doOnError(error -> System.out.println("Error fetching question: " + error));
    }

    @GetMapping()
    public Flux<QuestionResponseDTO> getAllQuestions(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return questionService.getAllQuestions(cursor, size)
                .doOnError(error -> System.out.println("Error fetching questions: " + error))
                .doOnComplete(() -> System.out.println("Questions fetched successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public Mono<Void> deleteQuestionById(@PathVariable String id) {
        return questionService.deleteQuestionById(id)
                .doOnSuccess(response -> System.out.println("Question Deleted successfully: " + response))
                .doOnError(error -> System.out.println("Error Deleting question: " + error));
    }

    @GetMapping("/search")
    public Flux<QuestionResponseDTO> searchQuestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "10") int size
    ) {
        return questionService.searchQuestions(query, pageOffset, size);
    }

    @GetMapping("/tag/{tag}")
    public Mono<PaginationResponseDTO<QuestionResponseDTO>> getQuestionsByTag(@PathVariable String tag,
                                                                              @RequestParam(defaultValue = "0") int pageOffset,
                                                                              @RequestParam(defaultValue = "10") int size
    ) {
        return questionService.getQuestionsByTag(tag , pageOffset , size)
                .doOnError(error -> System.out.println("Error fetching questions with request to tags: " + error))
                .doOnSuccess(response -> System.out.println("Questions fetched successfully with request to tags"));
    }



}

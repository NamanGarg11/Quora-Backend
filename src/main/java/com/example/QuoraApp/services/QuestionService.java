package com.example.QuoraApp.services;

import com.example.QuoraApp.dto.PaginationInfoDTO;
import com.example.QuoraApp.dto.PaginationResponseDTO;
import com.example.QuoraApp.dto.QuestionRequestDTO;
import com.example.QuoraApp.dto.QuestionResponseDTO;
import com.example.QuoraApp.enums.TargetType;
import com.example.QuoraApp.events.ViewCountEvent;
import com.example.QuoraApp.mapper.QuestionMapper;
import com.example.QuoraApp.models.Question;
import com.example.QuoraApp.producers.KafkaEventProducer;
import com.example.QuoraApp.repositories.IQuestionRepository;
import com.example.QuoraApp.utils.CursorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class QuestionService implements IQuestionService{

    private final IQuestionRepository questionRepository;
    private final KafkaEventProducer kafkaEventProducer;


    @Override
    public Mono<QuestionResponseDTO> createQuestion(QuestionRequestDTO questionRequestDTO) {
        Question question = Question.builder()
                .title(questionRequestDTO.getTitle())
                .content(questionRequestDTO.getContent())
//                .tags(questionRequestDTO.getTags())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return questionRepository.save(question)
            .map(QuestionMapper::toQuestionResponseDTO)
//                .map(savedQuestion -> {
//                    questionElasticSearchIndexService.createQuestionIndex(savedQuestion); // dumping the question to elasticsearch
//            return QuestionMapper.toQuestionResponseDTO(savedQuestion);
//        })
            .doOnSuccess(response -> System.out.println("Question created successfully: " + response))
            .doOnError(error -> System.out.println("Error creating question: " + error));
    }

    @Override
    public Flux<QuestionResponseDTO> searchQuestions(String query, int pageOffset, int size) {
        return questionRepository.findByTitleOrContentContainingIgnoreCase(query , PageRequest.of(pageOffset , size))
                .map(QuestionMapper::toQuestionResponseDTO)
                .doOnError(error -> System.out.println("Error searching questions: " + error))
                .doOnComplete(() -> System.out.println("Questions searched successfully"));
    }

    @Override
    public Flux<QuestionResponseDTO> getAllQuestions(String cursor, int size) {
        Pageable pageable = PageRequest.of(0 , size);
        if(!CursorUtils.isValidCursor(cursor)) {
            return questionRepository.findTop10ByOrderByCreatedAtAsc()
                    .take(size)
                    .map(QuestionMapper::toQuestionResponseDTO)
                    .doOnError(error -> System.out.println("Error fetching questions: " + error))
                    .doOnComplete(() -> System.out.println("Questions fetched successfully"));
        } else {
            LocalDateTime cursorTimeStamp = CursorUtils.parseCursor(cursor);
            return questionRepository.findByCreatedAtGreaterThanOrderByCreatedAtAsc(cursorTimeStamp, pageable)
                    .map(QuestionMapper::toQuestionResponseDTO)
                    .doOnError(error -> System.out.println("Error fetching questions: " + error))
                    .doOnComplete(() -> System.out.println("Questions fetched successfully"));
        }

    }

    @Override
    public Mono<QuestionResponseDTO> getQuestionById(String id) {
//        return questionRepository.findById(id)
//                .map(QuestionMapper::toQuestionResponseDTO)
//                .doOnSuccess(response -> System.out.println("Question Fetched successfully: " + response))
//                .doOnError(error -> System.out.println("Error Fetching question: " + error));
        return questionRepository.findById(id)
                .map(QuestionMapper::toQuestionResponseDTO)
                .doOnError(error -> System.out.println("Error fetching question: " + error))
                .doOnSuccess(response -> {
                    System.out.println("Question fetched successfully: " + response);
                    ViewCountEvent viewCountEvent = new ViewCountEvent(id, TargetType.Question, LocalDateTime.now());
                    kafkaEventProducer.publishViewCountEvent(viewCountEvent);
                });
    }

    @Override
    public Mono<Void> deleteQuestionById(String id) {
        return questionRepository.deleteById(id)
                .doOnSuccess(response -> System.out.println("Question Fetched successfully: " + response))
                .doOnError(error -> System.out.println("Error Fetching question: " + error));
    }

    @Override
    public Mono<PaginationResponseDTO<QuestionResponseDTO>> getQuestionsByTag(String tag, int pageOffset, int size) {

        Pageable pageable = PageRequest.of(pageOffset , size);

        // get the page data
        Mono<List<QuestionResponseDTO>> MainData = questionRepository.findByTagsContainingIgnoreCase(tag , pageable)
                .map(QuestionMapper::toQuestionResponseDTO)
                .collectList();

        // get the total page values
        Mono<Long> countData = questionRepository.countByTag(tag);

        // combine both results
        return Mono.zip(MainData, countData, (data, totalRecords) -> {

            long totalPages = (totalRecords + size - 1) / size; // Calculate total pages

            // Build the pagination info block
            PaginationInfoDTO paginationInfo = PaginationInfoDTO.builder()
                    .totalRecords(totalRecords)
                    .currentPage(pageOffset + 1) // pageOffset is 0-indexed, so we add 1 for display
                    .totalPages((int) totalPages)
                    .nextPage(pageOffset + 1 < totalPages ? pageOffset + 2 : null)
                    .prevPage(pageOffset > 0 ? pageOffset : null)
                    .build();

            // Build the final response object
            return PaginationResponseDTO.<QuestionResponseDTO>builder()
                    .data(data)
                    .pagination(paginationInfo)
                    .build();
        });

    }


}

package com.example.QuoraApp.mapper;

import com.example.QuoraApp.dto.QuestionResponseDTO;
import com.example.QuoraApp.models.Question;

public class QuestionMapper {

    public static QuestionResponseDTO toQuestionResponseDTO(Question question) {
        return QuestionResponseDTO.builder()
                .id(question.getId())
                .title(question.getTitle())
//                .tags(question.getTags())
                .content(question.getContent())
//                .views(question.getViews())
                .createdAt(question.getCreatedAt())
                .build();
    }
}

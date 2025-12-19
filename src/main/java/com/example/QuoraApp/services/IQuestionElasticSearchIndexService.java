package com.example.QuoraApp.services;


import com.example.QuoraApp.models.Question;

public interface IQuestionElasticSearchIndexService {
    void createQuestionIndex(Question question);
}

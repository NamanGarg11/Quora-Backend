package com.example.QuoraApp.repositories;

import com.example.QuoraApp.models.Answer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAnswerRepository extends ReactiveMongoRepository<Answer , String> {
}

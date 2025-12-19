package com.example.QuoraApp.repositories;

import com.example.QuoraApp.models.Like;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ILikeRepository extends ReactiveMongoRepository<Like , String> {

}

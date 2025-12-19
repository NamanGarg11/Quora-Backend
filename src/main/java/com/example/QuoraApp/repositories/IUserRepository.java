package com.example.QuoraApp.repositories;

import com.example.QuoraApp.models.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends ReactiveMongoRepository<User , String> {

}

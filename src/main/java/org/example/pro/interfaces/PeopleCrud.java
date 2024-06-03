package org.example.pro.interfaces;

import org.example.pro.entities.PeopleEntity;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public  interface PeopleCrud extends ReactiveMongoRepository<PeopleEntity,String> {
    @Query("{ 'country': ?0, 'criteria': 'country' }")
    Flux<PeopleEntity> findByCountryAndCriteria(String country, String criteria);
}




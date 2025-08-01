package org.example.pro.interfaces;

import org.example.pro.boundries.PeopleBoundary;
import org.example.pro.entities.PeopleEntity;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Optional;

public  interface PeopleCrud extends ReactiveMongoRepository<PeopleEntity,String> {

    Flux<PeopleEntity> findByCountry(String country);
    Flux<PeopleEntity> findByLast(String value);

    Flux<PeopleEntity> findByBirthdateBetween(LocalDate minDate, LocalDate maxDate);

    Flux<PeopleEntity> findByEmail(String email);
}




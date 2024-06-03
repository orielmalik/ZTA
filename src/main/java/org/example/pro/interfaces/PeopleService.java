package org.example.pro.interfaces;

import org.example.pro.boundries.PeopleBoundary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PeopleService {


    Mono<PeopleBoundary> create(PeopleBoundary boundary);
    Flux<PeopleBoundary> getPeopleByCountry (String country,String criteria);

    Mono<Void> deleteAll();

    Mono<Void> update(String email, String password,PeopleBoundary peopleBoundary);

    Flux<PeopleBoundary> getAll();
}

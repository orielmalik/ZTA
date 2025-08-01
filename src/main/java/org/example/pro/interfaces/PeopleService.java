package org.example.pro.interfaces;

import org.example.pro.boundries.PeopleBoundary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PeopleService {


    Mono<PeopleBoundary> create(PeopleBoundary boundary);
    Flux<PeopleBoundary> getPeopleByCountry (String country);

    Mono<Void> deleteAll();

    Mono<Void> update(String email, String password,PeopleBoundary peopleBoundary);

    Flux<PeopleBoundary> getAll();

    Flux<PeopleBoundary> getByLastName( String value);

    Mono<PeopleBoundary> getByEmail(String email, String password);

    Flux<PeopleBoundary> getPeopleByMinimumAge(int value);

    Flux<PeopleBoundary> getPeopleByMaximumAge(int value);


    Flux<PeopleBoundary> getByEmailOnly(String value);
}

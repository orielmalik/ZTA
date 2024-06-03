package org.example.pro.interfaces;

import org.example.pro.boundries.PeopleBoundary;
import reactor.core.publisher.Mono;

public interface PeopleService {


    Mono<PeopleBoundary> create(PeopleBoundary boundary);
}

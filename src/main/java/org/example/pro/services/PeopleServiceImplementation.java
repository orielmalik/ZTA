package org.example.pro.services;

import org.example.pro.boundries.PeopleBoundary;
import org.example.pro.interfaces.PeopleCrud;
import org.example.pro.interfaces.PeopleService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PeopleServiceImplementation implements PeopleService {
private PeopleCrud peopleCrud;
public PeopleServiceImplementation( PeopleCrud peopleCrud )
{
    this.peopleCrud=peopleCrud;
}
    @Override
    public Mono<PeopleBoundary> create(PeopleBoundary boundary) {
        return Mono.just(boundary)
                .map(b->{
                    b.setEmail(null);
                    //TODO BAR LIWWA VALIDATION OF INPUT
                    return b;
                })
                .map(PeopleBoundary::toEntity)
                .flatMap(this.peopleCrud::save)
                .map(PeopleBoundary::new)
                .log();
    }
}

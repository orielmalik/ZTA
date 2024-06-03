package org.example.pro.services;

import org.example.pro.Exception.BadRequest400;
import org.example.pro.boundries.PeopleBoundary;
import org.example.pro.interfaces.PeopleCrud;
import org.example.pro.interfaces.PeopleService;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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
        //TODO LIWA BAR

        return Mono.just(boundary)
                .map(b -> {
                    b.setEmail(boundary.getEmail());
                    b.setAddress(boundary.getAddress());
                    b.setPassword(b.getPassword());
                    return b;
                })
                .map(PeopleBoundary::toEntity)
                .flatMap(this.peopleCrud::save)
                .map(PeopleBoundary::new)
                .log();
    }

    @Override
    public Flux<PeopleBoundary> getPeopleByCountry(String country, String criteria) {
        return this.peopleCrud.findByCountryAndCriteria(country,criteria)
                .flatMap(object -> this.peopleCrud.findById(object.getEmail()))
                .map(PeopleBoundary::new)
                .log();
    }

    @Override
    public Mono<Void> deleteAll() {
        return this.peopleCrud.deleteAll();
    }


    @Override
    public Mono<Void> update( String email, String password, PeopleBoundary peopleBoundary) {
        return this.peopleCrud.findById(email)
                .flatMap(peopleEntity -> {
                    if(!peopleEntity.getPassword().equals(password))
                    {
                        return Mono.error(()->new BadRequest400("password does not match"));
                    }else {
                        peopleBoundary.setEmail(peopleEntity.getEmail());//cant change email

                        return this.peopleCrud.save(peopleBoundary.toEntity());
                    }

                })
                .then().log();
    }

    @Override
    public Flux<PeopleBoundary> getAll() {
        return this.peopleCrud.findAll().map(peopleEntity -> {
            peopleEntity.setPassword("******");
            return new PeopleBoundary(peopleEntity);
        }).log();
    }


}


package org.example.pro.controller;

import org.example.pro.boundries.PeopleBoundary;
import org.example.pro.interfaces.PeopleService;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/people")
public class PeopleController {

    private final PeopleService peopleService;

    public PeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PeopleBoundary> create(@RequestBody PeopleBoundary boundary) {
        return this.peopleService.create(boundary);
    }

    /*  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PeopleBoundary> getAllByCountry(@RequestParam("criteria") String criteria,
                                                @RequestParam("countryCode") String countryCode) {
        return this.peopleService.getPeopleByCountry(countryCode,criteria);
    }*/
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PeopleBoundary> getAll() {
        return this.peopleService.getAll();
    }

    @DeleteMapping
    public Mono<Void> delete(){
        return this.peopleService
                .deleteAll();
    }



    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Void>updatePeople( @RequestParam ("email") String email,
                                  @RequestParam ("password") String password,
                                  @RequestBody PeopleBoundary update
    )
    {
        return  this.peopleService.update(email,password,update);
    }

}

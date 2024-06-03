package org.example.pro.controller;
import org.example.pro.boundries.PeopleBoundary;
import org.example.pro.interfaces.PeopleService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = { "/people" })
public class PeopleController {

    private PeopleService peopleService;


    public PeopleController(PeopleService objectService) {
        super();
        this.peopleService = objectService;
    }

    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<PeopleBoundary> create(
            @RequestBody PeopleBoundary boundary){
        return this.peopleService
                .create(boundary);
    }




}

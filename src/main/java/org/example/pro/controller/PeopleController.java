package org.example.pro.controller;

import org.example.pro.boundries.PeopleBoundary;
import org.example.pro.interfaces.PeopleService;
import org.example.pro.services.JwtService;
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
    private final JwtService jwtService;


    public PeopleController(PeopleService peopleService, JwtService jwtService) {
        this.peopleService = peopleService;
        this.jwtService = jwtService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> create(@RequestBody PeopleBoundary boundary) {
        return this.peopleService.create(boundary)
                .flatMap(peopleBoundary -> { return this.jwtService.generateToken(peopleBoundary);});
    }
  @PostMapping(path = "/log",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> createLogin(@RequestBody PeopleBoundary boundary) {
        return this.peopleService.getByEmail(boundary.getEmail(),boundary.getPassword())
                .flatMap(peopleBoundary -> { return this.jwtService.generateToken(peopleBoundary);});
    }




    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PeopleBoundary> getAllByCriteria(@RequestParam(value = "criteria",required = false)  String criteria,
                                       @RequestParam(value = "value",required = false) String value) {
        switch (criteria)
        {
            case "country":
                return this.peopleService.getPeopleByCountry(value);
            case "last":
                return  this.peopleService.getByLastName(value);
            case "maximumAge":
                return  this.peopleService.getPeopleByMaximumAge(Integer.parseInt(value));
            case "minimumAge":
                return  this.peopleService.getPeopleByMinimumAge(Integer.parseInt(value));

            case "email":
                return this.peopleService.getByEmailOnly(value);

            case null, default:return  this.peopleService.getAll();
        }

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



@GetMapping
        (produces = {MediaType.APPLICATION_JSON_VALUE},path = {"/{email}"})
    public  Mono<PeopleBoundary>getByPassword(@PathVariable("email") String email,
                                              @RequestParam ("password")String password)
{
    return  this.peopleService.getByEmail(email,password);//with password match
}


}

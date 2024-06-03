package org.example.pro.interfaces;

import org.example.pro.entities.PeopleEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public  interface PeopleCrud extends ReactiveMongoRepository<PeopleEntity,String> {

}

package org.example.pro.boundries;

import org.example.pro.entities.PeopleEntity;
import org.example.pro.tools.Address;
import org.example.pro.tools.Name;

import java.time.LocalDate;

public class PeopleBoundary {
    private String email;
    private Address address;
    private  String[] roles;
    private LocalDate birthdate;
    private  String password;

    private Name name;

    public PeopleBoundary()
    {
    }



    public PeopleBoundary(PeopleEntity entity) {
this.setBirthdate(entity.getBirthdate());
this.setEmail(entity.getEmail());
this.setPassword(entity.getPassword());
this.setName(new Name(entity.getName().getFirst(),entity.getName().getLast()));
this.setAddress(new Address(entity.getAddress().getCountry(),entity.getAddress().getCity()
,entity.getAddress().getZip()));

    }

    public PeopleEntity toEntity()
    {
        PeopleEntity peopleEntity=new PeopleEntity();
        peopleEntity.setEmail(getEmail());
        peopleEntity.setAddress(getAddress());
        peopleEntity.setRoles(getRoles());
        peopleEntity.setBirthdate(getBirthdate());
        peopleEntity.setName(getName());

        return peopleEntity;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }
}

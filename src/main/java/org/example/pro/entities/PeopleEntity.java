package org.example.pro.entities;

import org.example.pro.tools.Address;
import org.example.pro.tools.Name;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "people")
public class PeopleEntity {


    @Id
    private String email;
    private Address address;
    private  String[] roles;
    private LocalDate birthdate;
    private  String password;

    private Name name;

    public PeopleEntity()
    {

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

package org.example.pro.tools;

public class Address {

    private String city;
    private String country;
    private String zip;

    public Address(String country, String city, String zip)
    {
        this.country=country;
        this.city=city;
        this.zip=zip;
    }



    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }


    @Override
    public String toString() {
        return   "address"+"country:"+this.country+ "city:"+this.city+ "zip"+"1234567" ;}


}

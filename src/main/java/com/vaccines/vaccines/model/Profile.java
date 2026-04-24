package com.vaccines.vaccines.model;
import java.time.LocalDate;
import java.util.ArrayList;

public class Profile {
    private ProfileType type;
    private String name;
    private ArrayList<Vaccination> vaccines;
    private LocalDate dateOfBirth;

    public Profile(ProfileType type, String name){
        this.type = type;
        this.name = name;
        vaccines = new ArrayList<Vaccination>();
    }
    public ProfileType getType(){
        return type;
    }
    public void setType(ProfileType type){
        this.type = type;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public ArrayList<Vaccination> getVaccines(){
        return vaccines;
    }
    public void setVaccines(ArrayList<Vaccination> vaccine){
        this.vaccines = vaccine;
    }
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}

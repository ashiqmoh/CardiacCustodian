package de.hfu.ashiqmoh.cardiaccustodian.objects;

import java.util.ArrayList;
import java.util.Date;

import de.hfu.ashiqmoh.cardiaccustodian.enums.Gender;

public class User {

    private String id;
    private Gender gender;
    private String firstName;
    private String lastName;
    private ArrayList<String> diseases;
    private String helpContact;
    private Date birthday;
    private Location location;

    public User(String id, Gender gender, String firstName, String lastName, ArrayList<String> diseases, Date birthday, String helpContact, Location location) {
        this.id = id;
        this.gender = gender;
        this.firstName = firstName;
        this.lastName = lastName;
        this.diseases = diseases;
        this.birthday = birthday;
        this.helpContact = helpContact;
        this.location = location;
    }
}

package net.gcuisinier.devoxx2019.webauthnbackend.model;

import lombok.Data;

@Data
public class UserRegistration {


    String username;

    String firstName;

    String lastName;

    String email;
}

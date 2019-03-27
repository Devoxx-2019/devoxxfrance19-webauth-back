package net.gcuisinier.devoxx2019.webauthnbackend.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfile {

    String name;

    String email;

    String usedKey;



}

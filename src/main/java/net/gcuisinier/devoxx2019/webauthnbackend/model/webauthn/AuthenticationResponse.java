package net.gcuisinier.devoxx2019.webauthnbackend.model.webauthn;

import lombok.Data;

@Data
public class AuthenticationResponse {

    String id;
    String rawId;


    String username;

    AuthenticatorAssertionResponse response;


}

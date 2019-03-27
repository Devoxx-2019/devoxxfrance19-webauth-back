package net.gcuisinier.devoxx2019.webauthnbackend.model.webauthn;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder(toBuilder = true)
public class AuthenticationRequestOptions {

    String challenge;

    String rpId;


    Set<Credential> allowCredentials;

    long timeout;

}

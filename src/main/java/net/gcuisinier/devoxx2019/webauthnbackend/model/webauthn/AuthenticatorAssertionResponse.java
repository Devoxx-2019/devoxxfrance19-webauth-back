package net.gcuisinier.devoxx2019.webauthnbackend.model.webauthn;

import lombok.Builder;
import lombok.Data;


@Data
public class AuthenticatorAssertionResponse {

    String authenticatorData;
    String clientDataJSON;

    String signature;
    String userHandle;
}

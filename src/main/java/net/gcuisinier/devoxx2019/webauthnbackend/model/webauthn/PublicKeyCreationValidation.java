package net.gcuisinier.devoxx2019.webauthnbackend.model.webauthn;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PublicKeyCreationValidation {

    String id;

    String rawId;

    String attestation;

    String clientData;

    String username;

}

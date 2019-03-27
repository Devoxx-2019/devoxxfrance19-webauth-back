package net.gcuisinier.devoxx2019.webauthnbackend.model.webauthn;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName = "newPublicKeyParam")
public class PublicKeyParam {
    String type;

    Integer alg;
}

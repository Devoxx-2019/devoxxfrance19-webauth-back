package net.gcuisinier.devoxx2019.webauthnbackend.model.webauthn;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName = "newRP")
public class RelayingPartyIdentity {

    String id;

    String name;
}

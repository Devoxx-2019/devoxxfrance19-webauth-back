package net.gcuisinier.devoxx2019.webauthnbackend.model.webauthn;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder(toBuilder = true)
public class PublicKeyCredentialCreationOptions {

    private RelayingPartyIdentity rp;

    private User user;

    private Set<PublicKeyParam> pubKeyCredParams;

    private String challenge;

    private Integer timeout = 10000;

    private String attestaton = "none";
}

package net.gcuisinier.devoxx2019.webauthnbackend.model;

import com.webauthn4j.data.attestation.authenticator.CredentialPublicKey;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoredKey {

    byte[] credentialId;

    CredentialPublicKey publicKey;

    long signCount;

}

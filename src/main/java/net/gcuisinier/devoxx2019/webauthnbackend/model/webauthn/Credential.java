package net.gcuisinier.devoxx2019.webauthnbackend.model.webauthn;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Credential {

    String id;

    String type;


}

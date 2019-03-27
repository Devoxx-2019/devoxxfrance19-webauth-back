package net.gcuisinier.devoxx2019.webauthnbackend.model.webauthn;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class User {

    String id;

    String name;

    String displayName;

}

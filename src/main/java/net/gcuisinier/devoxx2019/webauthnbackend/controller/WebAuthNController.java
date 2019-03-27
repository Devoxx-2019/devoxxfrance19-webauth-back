package net.gcuisinier.devoxx2019.webauthnbackend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webauthn4j.converter.AttestationObjectConverter;
import com.webauthn4j.converter.AuthenticatorDataConverter;
import com.webauthn4j.converter.util.CborConverter;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.webauthn4j.data.attestation.authenticator.CredentialPublicKey;
import com.webauthn4j.data.extension.authenticator.ExtensionAuthenticatorOutput;
import com.webauthn4j.util.MessageDigestUtil;
import net.gcuisinier.devoxx2019.webauthnbackend.UserService;
import net.gcuisinier.devoxx2019.webauthnbackend.model.StoredKey;
import net.gcuisinier.devoxx2019.webauthnbackend.model.UserProfile;
import net.gcuisinier.devoxx2019.webauthnbackend.model.webauthn.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.*;


@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class WebAuthNController {

    Map<String, String> registrationChallenges = new HashMap<>();
    Map<String, String> loginChallenges = new HashMap<>();

    @Autowired
    UserService userService;


    @GetMapping("/register")
    public PublicKeyCredentialCreationOptions registrerPhaseOne(@RequestHeader(value="Referer", required = false) URL referer, String username) throws MalformedURLException {

        String rpHost = referer!=null?referer.getHost():"local.gcuisinier.net";


        String challenge = generateChallenge();

        registrationChallenges.put(challenge, username);


        PublicKeyCredentialCreationOptions option = PublicKeyCredentialCreationOptions.builder()
                .rp(RelayingPartyIdentity.newRP()
                        .id(rpHost)
                        .name("Devoxx France")
                        .build())

                .pubKeyCredParams(Set.of(
                        PublicKeyParam.newPublicKeyParam()      //
                                .type("public-key")
                                .alg(-7)
                                .build(),
                        PublicKeyParam.newPublicKeyParam()
                                .type("public-key")
                                .alg(-257)
                                .build()))
                .user(User.builder()
                        .displayName(username)
                        .name(username)
                        .id(base64(username))
                        .build()

                )

                .timeout(60000)
                .challenge(challenge)
                .build();


        return option;


    }


    @PostMapping("/register")
    public UserProfile registrerPhaseTwo(@RequestBody PublicKeyCreationValidation attestationRaw) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode clientDataJSON = mapper.readTree(decodeBase64(attestationRaw.getClientData()));

        AttestationObject attestation = parseCBOR(attestationRaw);

        // Check Challenge
        String username = checkChallenge(clientDataJSON.get("challenge"));

        if (username != null) {
            long signCountForThisKey = attestation.getAuthenticatorData().getSignCount();
            byte[] credentialId = Base64.getUrlDecoder().decode(attestationRaw.getId());
            CredentialPublicKey publicKey = attestation.getAuthenticatorData().getAttestedCredentialData().getCredentialPublicKey();

            userService.createUser(username);
            userService.createKeyForUser(username, credentialId, publicKey, signCountForThisKey);

        }

        return UserProfile.builder()
                .name(username).usedKey(attestationRaw.getId())
                .build();

    }


    @PostMapping("/login")
    public UserProfile loginPhase2(@RequestBody AuthenticationResponse authenticationResponse) {

        byte[] clientData = decodeBase64(authenticationResponse.getResponse().getClientDataJSON());
        byte[] signature = decodeBase64(authenticationResponse.getResponse().getSignature());
        byte[] authData = decodeBase64(authenticationResponse.getResponse().getAuthenticatorData());
        String user = new String(decodeBase64(authenticationResponse.getResponse().getUserHandle()));
        if(user.equals("")) user = authenticationResponse.getUsername();
        String keyId = authenticationResponse.getId();

        AuthenticatorDataConverter converter = new AuthenticatorDataConverter(new CborConverter());

        AuthenticatorData<ExtensionAuthenticatorOutput> authDataParsed = converter.convert(authData);

        byte[] signedData = getSignedData(clientData, authData);

        StoredKey keyFromDb = userService.getPublicKeyFromDb(user, decodeBase64(authenticationResponse.getId()), authDataParsed.getSignCount());

        boolean signCountChecked = authDataParsed.getSignCount() > keyFromDb.getSignCount();
        boolean signatureValid = keyFromDb.getPublicKey().verifySignature(signature, signedData);


        if (signCountChecked && signatureValid)
            return UserProfile.builder()
                    .name(user).usedKey(keyId)
                    .build();

        else throw new RuntimeException("Unable to valid");

    }


    @GetMapping("/login")
    public AuthenticationRequestOptions loginPhase1(@RequestHeader(value="Referer", required = false) URL referer, String username) {

        String rpHost = referer!=null?referer.getHost():"local.gcuisinier.net";


        String challenge = generateChallenge();
        loginChallenges.put(challenge, username);

        Set<Credential> credentialsId = userService.getCredentialsIdFromUser(username);

        return AuthenticationRequestOptions.builder()
                .challenge(challenge)
                .timeout(60000)
                .allowCredentials(credentialsId)
                .rpId(rpHost)
                .build();

    }


    // Utility functions

    private AttestationObject parseCBOR(@RequestBody PublicKeyCreationValidation attestation) {
        AttestationObjectConverter converter = new AttestationObjectConverter(new CborConverter());
        return converter.convert(attestation.getAttestation());
    }


    private String checkChallenge(JsonNode challenge) {

        String challengeFromAttestation = challenge.textValue().replaceAll("\"", "");

        String username = registrationChallenges.remove(challengeFromAttestation);
        if (username == null) username = registrationChallenges.remove(challengeFromAttestation + "==");
        if (username == null) {
            throw new RuntimeException("Invalid challenge");
        }

        System.out.println("Username found  : " + username);
        return username;

    }


    private String generateChallenge() {

        byte[] challenge = new byte[16];

        new Random().nextBytes(challenge);

        return Base64.getUrlEncoder().encodeToString(challenge);


    }

    private String base64(String stringToEncode) {
        return Base64.getUrlEncoder().encodeToString(stringToEncode.getBytes());
    }

    private byte[] decodeBase64(String encoded) {
        return Base64.getUrlDecoder().decode(encoded);
    }


    private byte[] getSignedData(byte[] clientData, byte[] authenticator) {
        MessageDigest messageDigest = MessageDigestUtil.createSHA256();
        byte[] rawAuthenticatorData = authenticator;
        byte[] clientDataHash = messageDigest.digest(clientData);
        return ByteBuffer.allocate(rawAuthenticatorData.length + clientDataHash.length).put(rawAuthenticatorData).put(clientDataHash).array();
    }


}

package net.gcuisinier.devoxx2019.webauthnbackend;

import com.webauthn4j.data.attestation.authenticator.CredentialPublicKey;
import net.gcuisinier.devoxx2019.webauthnbackend.controller.NotFoundException;
import net.gcuisinier.devoxx2019.webauthnbackend.model.StoredKey;
import net.gcuisinier.devoxx2019.webauthnbackend.model.webauthn.Credential;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class UserService {

    Map<String, Map<String, Object>> data = new HashMap<>();



    public void createUser(String username){


        Map<String, Object> userData = new HashMap<>();
        data.putIfAbsent(username, userData);


    }

    public void createKeyForUser(String username, byte[] credentialId, CredentialPublicKey publicKey, long signCountForThisKey) {
        Map<String, Object> userData = data.get(username);


        Set<StoredKey> keys = (Set<StoredKey>) userData.getOrDefault("keys", new HashSet<StoredKey>());


        StoredKey key = StoredKey.builder()
                .credentialId(credentialId).publicKey(publicKey).signCount(signCountForThisKey)
                .build();

        keys.add(key);

        userData.putIfAbsent("keys", keys);


    }

    public Set<Credential> getCredentialsIdFromUser(String username) {

        Map<String, Object> userData = data.get(username);
        if(userData == null) throw new NotFoundException();

        Set<StoredKey> storedKeys = (Set<StoredKey>) userData.get("keys");


       return  storedKeys.stream().map( key -> {
            return  Credential.builder().id(Base64.getUrlEncoder().encodeToString(key.getCredentialId()))
                    .type("public-key").build();

        } ).collect(Collectors.toSet());




    }

    public StoredKey getPublicKeyFromDb(String username, byte[] keyId, long currentCount){

        Map<String, Object> userData = data.get(username);
        if(userData == null) throw new NotFoundException();

        Set<StoredKey> storedKeys = (Set<StoredKey>) userData.get("keys");

        StoredKey foundKey = null ;

        for(StoredKey currentKey: storedKeys){
            if(Arrays.equals(currentKey.getCredentialId(), keyId)){
                foundKey = currentKey;
                break;
            }
        }


       return foundKey;


    }
}

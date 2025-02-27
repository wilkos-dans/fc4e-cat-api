package org.grnet.cat.api.utils;

import io.quarkus.oidc.TokenIntrospection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class Utility {

    /**
     * Injection point for the Token Introspection
     */
    @Inject
    TokenIntrospection tokenIntrospection;

    @ConfigProperty(name = "oidc.user.unique.id")
    String key;

    public String getUserUniqueIdentifier(){

        String id;

        try{

            id = tokenIntrospection.getJsonObject().getString(key);
        } catch (Exception e){

            String message = String.format("The User's unique identifier {%s} is missing from the access token.", key);
            throw new BadRequestException(message);
        }

        return id;
    }
}

package org.example.canvaslearningassistant.utils;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;

public class IdTokenClaimsUtils {
    public static Optional<String> getLtiContextId(String idToken) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(idToken);
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        Map<String, Object> ltiClaims = claims.getClaims();
        Object contextObj = ltiClaims.get("https://purl.imsglobal.org/spec/lti/claim/context");
        if (contextObj instanceof Map<?, ?> contextMap) {
            return Optional.of((String) contextMap.get("id"));
        }

        return Optional.empty();
    }

    public static Optional<Integer> getUserId(String idToken) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(idToken);
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        Map<String, Object> ltiClaims = claims.getClaims();
        Object contextObj = ltiClaims.get("https://purl.imsglobal.org/spec/lti/claim/custom");
        if (contextObj instanceof Map<?, ?> contextMap) {
            return Optional.of(Integer.parseInt((String)contextMap.get("canvas_user_id")));
        }

        return Optional.empty();
    }
}

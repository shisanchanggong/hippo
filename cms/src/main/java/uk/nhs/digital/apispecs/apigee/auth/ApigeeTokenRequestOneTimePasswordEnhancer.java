package uk.nhs.digital.apispecs.apigee.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.util.MultiValueMap;

import java.time.Clock;

public class ApigeeTokenRequestOneTimePasswordEnhancer implements RequestEnhancer {

    private static final String TOKEN_REQUEST_FIELD_NAME_MFA_TOKEN = "mfa_token";

    private final Clock clock = Clock.systemDefaultZone();

    private final GoogleAuthOtpGenerator otpGenerator;

    private String otpKey; // rktodo move to GoogleAuthOtpGenerator - but review the libraries first!

    public ApigeeTokenRequestOneTimePasswordEnhancer(final GoogleAuthOtpGenerator otpGenerator,
                                                     final String otpKey) {
        this.otpKey = otpKey;
        this.otpGenerator = otpGenerator;
    }

    @Override
    public void enhance(
        final AccessTokenRequest request,
        final OAuth2ProtectedResourceDetails resource,
        final MultiValueMap<String, String> form,
        final HttpHeaders headers
    ) {
        form.add(TOKEN_REQUEST_FIELD_NAME_MFA_TOKEN, otpGenerator.googleAuthenticatorCode(otpKey, clock));
    }
}

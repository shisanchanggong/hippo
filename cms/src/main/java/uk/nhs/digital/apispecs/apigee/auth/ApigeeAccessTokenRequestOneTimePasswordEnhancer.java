package uk.nhs.digital.apispecs.apigee.auth;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.RequestEnhancer;
import org.springframework.util.MultiValueMap;

import java.time.Clock;

public class ApigeeAccessTokenRequestOneTimePasswordEnhancer implements RequestEnhancer {

    private static final String TOKEN_REQUEST_FIELD_NAME_MFA_TOKEN = "mfa_token";
    private static final String HTTP_HEADER_NAME_AUTHORIZATION = "Authorization";

    private final Clock clock = Clock.systemDefaultZone();

    private final GoogleAuthenticator otpGenerator;

    private String otpKey;
    private final String basicAuthToken;

    public ApigeeAccessTokenRequestOneTimePasswordEnhancer(final GoogleAuthenticator otpGenerator,
                                                           final String otpKey,
                                                           final String basicAuthToken
    ) {
        this.otpKey = otpKey;
        this.otpGenerator = otpGenerator;
        this.basicAuthToken = basicAuthToken;
    }

    @Override
    public void enhance(
        final AccessTokenRequest request,
        final OAuth2ProtectedResourceDetails resource,
        final MultiValueMap<String, String> form,
        final HttpHeaders headers
    ) {
        final String authorisationHeaderValue = "Basic " + basicAuthToken;

        headers.add(HTTP_HEADER_NAME_AUTHORIZATION, authorisationHeaderValue);

        form.add(
            TOKEN_REQUEST_FIELD_NAME_MFA_TOKEN,
            oneTimePassword()
        );
    }

    private String oneTimePassword() {
        return String.valueOf(otpGenerator.getTotpPassword(otpKey, clock.millis()));
    }
}

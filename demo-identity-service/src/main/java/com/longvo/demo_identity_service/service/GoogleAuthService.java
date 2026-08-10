package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.repository.httpclient.OutboundIdentityClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GoogleAuthService {

    private final OutboundIdentityClient outboundIdentityClient;
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @NonFinal
    @Value("${google.client-id}")
    private String clientId;

    @NonFinal
    @Value("${google.client-secret}")
    private String clientSecret;

    @NonFinal
    @Value("${google.redirect-uri}")
    private String redirectUri;


    /*public String handleCodeAndReturnJwt(String code) throws Exception {
        // prepare form
        Map<String, String> form = Map.of(
                "code", code,
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", redirectUri,
                "grant_type", "authorization_code"
        );

        ExchangeTokenResponse tokenResp = outboundIdentityClient.exchangeToken(form);

        // verify id_token
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = GoogleIdToken.parse(new JacksonFactory(), tokenResp.getIdToken());
        if (!verifier.verify(idToken)) {
            throw new IllegalStateException("Invalid ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        // find or create user in your DB
        var user = userService.findOrCreateByGoogle(email, name, picture);

        // create your own JWT (application JWT)
        return authenticationService.generateToken(user);
    }*/
}

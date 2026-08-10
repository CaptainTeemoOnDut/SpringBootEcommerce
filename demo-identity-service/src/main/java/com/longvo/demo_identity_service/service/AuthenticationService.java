package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.constant.PredefinedRole;
import com.longvo.demo_identity_service.dto.request.*;
import com.longvo.demo_identity_service.dto.response.AuthenticationResponse;
import com.longvo.demo_identity_service.dto.response.IntrospectResponse;
import com.longvo.demo_identity_service.entity.RefreshToken;
import com.longvo.demo_identity_service.entity.Role;
import com.longvo.demo_identity_service.entity.User;
import com.longvo.demo_identity_service.exception.AppException;
import com.longvo.demo_identity_service.exception.ErrorCode;
import com.longvo.demo_identity_service.repository.*;
import com.longvo.demo_identity_service.repository.httpclient.OutboundIdentityClient;
import com.longvo.demo_identity_service.repository.httpclient.OutboundUserClient;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    IssuedTokenRepository issuedTokenRepository;
    RefreshTokenService refreshTokenService;
    RefreshTokenRepository refreshTokenRepository;
    RedisRefreshTokenService redisRefreshTokenService;
    OutboundIdentityClient outboundIdentityClient;
    OutboundUserClient outboundUserClient;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @NonFinal
    @Value("${google.client-id}")
    protected String CLIENT_ID;

    @NonFinal
    @Value("${google.client-secret}")
    protected String CLIENT_SECRET;

    @NonFinal
    @Value("${google.redirect-uri}")
    protected String REDIRECT_URI;

    @NonFinal
    protected final String GRANT_TYPE = "authorization_code";

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        SignedJWT jwt = null;
        try {
            jwt = verifyToken(token);
        } catch (AppException e) {
            isValid = false;
           throw e;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .userName(
                        Objects.nonNull(jwt)
                            ? jwt.getJWTClaimsSet().getSubject() : null
                )
                .build();
        //return IntrospectResponse.builder().build();
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {

        System.out.println("VERIFYING ACCESS TOKEN:" + token);

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        String userName = signedJWT.getJWTClaimsSet().getSubject();

        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.USER_NOT_ACTIVE);
        }

        if(!verified) {
            System.out.println("ACCESS TOKEN IS UNAUTHENTICATED");
            throw new AppException(ErrorCode.UNAUTHENTICATED); // Token giả mạo
        }

        if(!expiryTime.after(new Date())) {
            System.out.println("ACCESS TOKEN IS TOKEN_EXPIRED");
            throw new AppException(ErrorCode.TOKEN_EXPIRED); // Token hết hạn
        }

        return signedJWT;
    }

    public boolean validateRefreshToken(RefreshToken refreshToken) {

        return !refreshToken.getExpiryTime().before(new Date());
    }

    public AuthenticationResponse outboundAuthenticate(String code){
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        log.info("TOKEN RESPONSE {}", response);

        var userInfo = outboundUserClient.getUserInfo("Bearer " + response.getAccessToken());

        log.info("USER INFO {}", userInfo);

        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder().name(PredefinedRole.USER_ROLE).build());

        // Onboard user

        var user = userRepository.findByUsername(userInfo.getEmail()).orElseGet(
                () -> userRepository.save(User.builder()
                                .username(userInfo.getEmail())
                                .firstName(userInfo.getGivenName())
                                .lastName(userInfo.getFamilyName())
                                .avatarUrl(userInfo.getPicture())
                                .email(userInfo.getEmail())
                                .isActive(true)
                                .roles(roles)
                        .build())
        );

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .accessToken(token)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.USER_NOT_ACTIVE);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated =  passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        var accessToken = generateToken(user);

        //bo sung
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        redisRefreshTokenService.save(refreshToken.getToken(), user.getId());
        return AuthenticationResponse.builder()
                .userId(user.getId().toString())
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken()) // bo sung
                .authenticated(true)
                .build();
    }

    @Transactional
    public void logout(LogoutRequest request) {
        String refreshTokenStr = request.getToken();
        redisRefreshTokenService.delete(refreshTokenStr);

        // Find and delete refresh token
        refreshTokenRepository.findByToken(refreshTokenStr)
                .ifPresentOrElse(
                        refreshToken -> refreshTokenRepository.deleteById(refreshToken.getId()),
                        () -> {
                            throw new AppException(ErrorCode.UNAUTHENTICATED);
                        }
                );
    }


    public AuthenticationResponse refreshToken(String refreshTokenValue) {

        long start = System.currentTimeMillis();
        System.out.println("START refreshToken");
        String userId = redisRefreshTokenService.getUserIdByRefreshToken(refreshTokenValue);

        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        System.out.println("Step 1: getUserId done at " + (System.currentTimeMillis() - start) + "ms");


        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        System.out.println("Step 2: DB done at " + (System.currentTimeMillis() - start) + "ms");


        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);



        String accessToken = generateToken(user);
        System.out.println("Step 3: token created at " + (System.currentTimeMillis() - start) + "ms");


        redisRefreshTokenService.save(newRefreshToken.getToken(), Long.valueOf(userId));
        System.out.println("Step 4: Redis saved at " + (System.currentTimeMillis() - start) + "ms");

        long end = System.currentTimeMillis();
        System.out.println("⏱️ Refresh handled in: " + (end - start) + "ms");
        return AuthenticationResponse.builder()
                .userId(userId)
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .authenticated(true)
                .build();
    }

    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("longvo")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });

        return stringJoiner.toString();
    }
}

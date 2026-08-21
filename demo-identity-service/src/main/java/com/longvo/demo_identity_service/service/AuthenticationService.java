package com.longvo.demo_identity_service.service;

import com.longvo.demo_identity_service.configuration.PasswordEncoderConfig;
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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    RefreshTokenService refreshTokenService;
    RedisRefreshTokenService redisRefreshTokenService;
    OutboundIdentityClient outboundIdentityClient;
    OutboundUserClient outboundUserClient;
    PasswordEncoderConfig passwordEncoderConfig;

    @NonFinal
    @Value("${jwt.issuer}")
    protected String issuer;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.ttl}")
    protected Duration TTL;

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


    public IntrospectResponse introspect(IntrospectRequest request)  {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (AppException | JOSEException | ParseException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    private SignedJWT verifyToken(String token)
            throws JOSEException, ParseException {

        SignedJWT signedJWT = SignedJWT.parse(token);

        if (!JWSAlgorithm.HS512.equals(
                signedJWT.getHeader().getAlgorithm())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        JWSVerifier verifier =
                new MACVerifier(SIGNER_KEY.getBytes(StandardCharsets.UTF_8));

        if (!signedJWT.verify(verifier)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        Date expiryTime = claims.getExpirationTime();

        if (expiryTime == null || !expiryTime.after(new Date())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        if (!"longvo".equals(claims.getIssuer())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String userName = claims.getSubject();

        User user = userRepository.findByUsername(userName)
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.USER_NOT_ACTIVE);
        }

        return signedJWT;
    }

    public AuthenticationResponse outboundAuthenticate(String code){
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        var userInfo = outboundUserClient.getUserInfo("Bearer " + response.getAccessToken());

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

        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.USER_NOT_ACTIVE);
        }

        var accessToken = generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken();
        redisRefreshTokenService.save(refreshToken, user.getId(), TTL);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.USER_NOT_ACTIVE);
        }


        boolean authenticated =  passwordEncoderConfig.passwordEncoder().matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.INVALID_CREDENTIALS);

        var accessToken = generateToken(user);

        //bo sung
        String refreshToken = refreshTokenService.createRefreshToken();

        redisRefreshTokenService.save(refreshToken, user.getId(), TTL);

        return AuthenticationResponse.builder()
                .userId(user.getId().toString())
                .accessToken(accessToken)
                .refreshToken(refreshToken) // bo sung
                .authenticated(true)
                .build();
    }

    public void logout(LogoutRequest request) {

        String token = request.getToken();

        String userId =
                redisRefreshTokenService.getUserIdByRefreshToken(token);

        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        redisRefreshTokenService.delete(token);

    }


    public AuthenticationResponse refreshToken(String refreshTokenValue) {

        String userId =
                redisRefreshTokenService.getUserIdByRefreshToken(refreshTokenValue);

        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() ->
                        new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.USER_NOT_ACTIVE);
        }

        redisRefreshTokenService.delete(refreshTokenValue);

        String newRefreshToken =
                refreshTokenService.createRefreshToken();

        String accessToken = generateToken(user);

        redisRefreshTokenService.save(
                newRefreshToken,
                user.getId(),
                TTL
        );

        redisRefreshTokenService.save(newRefreshToken, Long.valueOf(userId), TTL);

        return AuthenticationResponse.builder()
                .userId(user.getId().toString())
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .authenticated(true)
                .build();
    }

    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer(issuer)
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
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes(StandardCharsets.UTF_8)));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new AppException(ErrorCode.TOKEN_GENERATION_FAILED);
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

package com.example.demo_database.service;

import com.example.demo_database.dto.request.AuthenticationRequest;
import com.example.demo_database.dto.request.IntrospectRequest;
import com.example.demo_database.dto.response.AuthenticationResponse;
import com.example.demo_database.dto.response.IntrospectResponse;
import com.example.demo_database.exception.ErrorCode;
import com.example.demo_database.exception.MyAppException;
import com.example.demo_database.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.shaded.gson.annotations.Since;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.persistence.Version;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    @NonFinal
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;
    public AuthenticationResponse authenticate(AuthenticationRequest request){
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new MyAppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated){
            throw new MyAppException(ErrorCode.UNAUTHENTICATED);
        }
        var token = generateToken(request.getUsername());
        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }
    public IntrospectResponse introspect(IntrospectRequest request){
        String token = request.getToken();
        try {
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean verify = signedJWT.verify(verifier);
            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
//            System.out.println(verify);
            return IntrospectResponse.builder()
                    .valid(verify && expiryTime.after(new Date()))
                    .build();

        } catch (JOSEException | ParseException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

    }
    private String generateToken(String username){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(username).issuer("ThienNhan")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .claim("customClaim", "customClaimValue")
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}

package com.example.demo_database.service;

import com.example.demo_database.dto.JwtObject.JwtInfo;
import com.example.demo_database.entity.User;
import com.example.demo_database.exception.ErrorCode;
import com.example.demo_database.exception.MyAppException;
import com.example.demo_database.redisRepository.RedisTokenRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final RedisTokenRepository redisTokenRepository;

    @NonFinal @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @NonFinal @Value("${jwt.valid-duration}")
    private long VALID_DURATION;
    @NonFinal @Value("${jwt.refreshable-duration}")
    private long REFRESHABLE_DURATION;

    public SignedJWT verifyToken(String token, boolean isRefresh)
            throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        boolean verify = signedJWT.verify(verifier);
        Date expiryTime = (isRefresh) ?
                new Date(signedJWT.getJWTClaimsSet().getIssueTime().
                        toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
//            System.out.println(verify);
        if(!(verify && expiryTime.after(new Date())) )
            throw new MyAppException(ErrorCode.UNAUTHENTICATED);
        if(redisTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()) ) // redisTokenRepo
            throw new MyAppException(ErrorCode.UNAUTHENTICATED);
        return signedJWT;
    }
    public String generateToken(User user, long timer){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        Date issueTime = new Date();
        Date expirationTime = new Date(issueTime.toInstant().plus(timer, ChronoUnit.SECONDS).toEpochMilli());
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .issuer("ThienNhan.com")
                .subject(user.getUsername())
                .issueTime(issueTime)
                .expirationTime(expirationTime)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
            // tạo ra 1 string là token từ 3 phần(header, payload, sig) đã có
        } catch (JOSEException e) {
            System.out.println(e.getMessage());
            throw new MyAppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
    public JwtInfo parseToken(String token)
            throws JOSEException, ParseException
    {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        boolean verify = signedJWT.verify(verifier);
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if(!(verify && expirationTime.after(new Date())) )
            throw new MyAppException(ErrorCode.UNAUTHENTICATED);
        return JwtInfo.builder()
                .jwtId(signedJWT.getJWTClaimsSet().getJWTID())
                .issueTime(signedJWT.getJWTClaimsSet().getIssueTime())
                .expirationTime(signedJWT.getJWTClaimsSet().getExpirationTime())
                .build();
    }
    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if(!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()) );
            });
        }
        return stringJoiner.toString();
    }

}

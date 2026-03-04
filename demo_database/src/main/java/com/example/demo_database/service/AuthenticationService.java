package com.example.demo_database.service;

import com.example.demo_database.dto.request.AuthenticationRequest;
import com.example.demo_database.dto.request.IntrospectRequest;
import com.example.demo_database.dto.request.LogoutRequest;
import com.example.demo_database.dto.request.RefreshTokenRequest;
import com.example.demo_database.dto.response.AuthenticationResponse;
import com.example.demo_database.dto.response.IntrospectResponse;
import com.example.demo_database.entity.InvalidatedToken;
import com.example.demo_database.entity.User;
import com.example.demo_database.exception.ErrorCode;
import com.example.demo_database.exception.MyAppException;
import com.example.demo_database.repository.InvalidatedTokenRepository;
import com.example.demo_database.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
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
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    @NonFinal
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    @NonFinal @Value("${jwt.valid-duration}")
    private long VALID_DURATION;
    @NonFinal @Value("${jwt.refreshable-duration}")
    private long REFRESHABLE_DURATION;

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new MyAppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated){
            throw new MyAppException(ErrorCode.UNAUTHENTICATED);
        }
        var token = generateToken(user, VALID_DURATION);
        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try{
            var signedToken = verifyToken(request.getToken(), true);
            // hàm này là logout thì không cần phải refreshToken do dù gì cũng logout, nhưng
            // cần đảm bảo được lưu vào invalidatedTokenRepository nên cần để thời gian hết hạn là thời gian refresh
            // điều này tránh việc token đã logout nhưng vẫn còn chưa invalidated
            String jti = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jti)
                    .expiryTime(expiryTime)
                    .build();
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (MyAppException exception){
            log.info("token has been expired");
        }

    }
    public IntrospectResponse introspect(IntrospectRequest request)
            throws ParseException, JOSEException {
        String token = request.getToken();
        boolean isValid = true;
        try{
            verifyToken(token, false);
        } catch (MyAppException e){
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }
    public AuthenticationResponse refreshToken(RefreshTokenRequest request)
            throws ParseException, JOSEException {
        var signedToken = verifyToken(request.getToken(), true);
        String jti = signedToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jti)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
        var userName = signedToken.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(userName).orElseThrow(
                () -> new MyAppException(ErrorCode.UNAUTHENTICATED)
        );
        var token = generateToken(user, VALID_DURATION);
        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
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
            if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()) )
                throw new MyAppException(ErrorCode.UNAUTHENTICATED);
            return signedJWT;
    }
    private String generateToken(User user, long timer){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername()).issuer("ThienNhan.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(timer, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            System.out.println(e.getMessage());
            throw new MyAppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
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

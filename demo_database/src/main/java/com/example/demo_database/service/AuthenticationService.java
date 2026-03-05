package com.example.demo_database.service;

import com.example.demo_database.dto.JwtObject.JwtInfo;
import com.example.demo_database.dto.JwtObject.RedisToken;
import com.example.demo_database.dto.request.AuthenticationRequest;
import com.example.demo_database.dto.request.IntrospectRequest;
import com.example.demo_database.dto.request.RefreshTokenRequest;
import com.example.demo_database.dto.response.AuthenticationResponse;
import com.example.demo_database.dto.response.IntrospectResponse;
import com.example.demo_database.dto.response.RefreshResponse;
import com.example.demo_database.entity.InvalidatedToken;
import com.example.demo_database.exception.ErrorCode;
import com.example.demo_database.exception.MyAppException;
import com.example.demo_database.repository.InvalidatedTokenRepository;
import com.example.demo_database.redisRepository.RedisTokenRepository;
import com.example.demo_database.repository.UserRepository;
import com.nimbusds.jose.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final JwtService jwtService;
    private final RedisTokenRepository redisTokenRepository;
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
        var accessToken = jwtService.generateToken(user, VALID_DURATION);
        var refreshToken = jwtService.generateToken(user, REFRESHABLE_DURATION);
        return AuthenticationResponse.builder()
                .authenticated(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    public void logout(String refreshToken) throws ParseException, JOSEException {
        try{
            JwtInfo jwtInfo = jwtService.parseToken(refreshToken);
            String jti = jwtInfo.getJwtId();
            Date issueTime = jwtInfo.getIssueTime();
            Date expiryTime = jwtInfo.getExpirationTime();
            Date now = new Date();
            Long ttl = Duration.between(now.toInstant(), expiryTime.toInstant()).getSeconds();
            if(expiryTime.after(now)){
                redisTokenRepository.save(RedisToken.builder()
                                .jwtId(jti)
                                .timeToLive(ttl)
                        .build());
            }
        } catch (MyAppException exception){
            log.info("token has been expired");
        }
    }
//    public void logout(LogoutRequest request) throws ParseException, JOSEException {
//        try{
//            var signedToken = jwtService.verifyToken(request.getToken(), true);
//            // hàm này là logout thì không cần phải refreshToken do dù gì cũng logout, nhưng
//            // cần đảm bảo được lưu vào invalidatedTokenRepository nên cần để thời gian hết hạn là thời gian refresh
//            // điều này tránh việc token đã logout nhưng vẫn còn chưa invalidated
//            String jti = signedToken.getJWTClaimsSet().getJWTID();
//            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();
//            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
//                    .id(jti)
//                    .expiryTime(expiryTime)
//                    .build();
//            invalidatedTokenRepository.save(invalidatedToken);
//        } catch (MyAppException exception){
//            log.info("token has been expired");
//        }
//
//    }
    public IntrospectResponse introspect(IntrospectRequest request)
            throws ParseException, JOSEException {
        String token = request.getToken();
        boolean isValid = true;
        try{
            jwtService.verifyToken(token, false);
        } catch (MyAppException e){
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }
    public RefreshResponse refreshToken(RefreshTokenRequest request)
            throws ParseException, JOSEException {
        var signedToken = jwtService.verifyToken(request.getToken(), true);
        String jti = signedToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();
        // thời gian hết hạn có thể sẽ không đúng với logic vì khi được refresh thì nó đã hết hạn ngay
        // việc này chỉ quyết định nếu có job dọn DB thì những token nào sẽ được dọn
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jti)
                .expiryTime(expiryTime)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
        var userName = signedToken.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(userName).orElseThrow(
                () -> new MyAppException(ErrorCode.UNAUTHENTICATED)
        );
        var accessToken = jwtService.generateToken(user, VALID_DURATION);
        return RefreshResponse.builder()
//                .authenticated(true)
                .accessToken(accessToken)
                .build();
    }
//    private SignedJWT verifyToken(String token, boolean isRefresh)
//            throws JOSEException, ParseException {
//        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
//        SignedJWT signedJWT = SignedJWT.parse(token);
//        boolean verify = signedJWT.verify(verifier);
//        Date expiryTime = (isRefresh) ?
//                new Date(signedJWT.getJWTClaimsSet().getIssueTime().
//                        toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
//                : signedJWT.getJWTClaimsSet().getExpirationTime();

//        if(!(verify && expiryTime.after(new Date())) )
//            throw new MyAppException(ErrorCode.UNAUTHENTICATED);
//        if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()) )
//            throw new MyAppException(ErrorCode.UNAUTHENTICATED);
//        return signedJWT;
//    }
//    private String generateToken(User user, long timer){
//        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
//        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
//                .subject(user.getUsername()).issuer("ThienNhan.com")
//                .issueTime(new Date())
//                .expirationTime(new Date(Instant.now().plus(timer, ChronoUnit.SECONDS).toEpochMilli()))
//                .jwtID(UUID.randomUUID().toString())
//                .claim("scope", buildScope(user))
//                .build();
//        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
//        JWSObject jwsObject = new JWSObject(header, payload);
//        try {
//            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
//            return jwsObject.serialize();
//            // tạo ra 1 string là token từ 3 phần(header, payload, sig) đã có
//        } catch (JOSEException e) {
//            System.out.println(e.getMessage());
//            throw new MyAppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
//        }
//    }
//    private String buildScope(User user){
//        StringJoiner stringJoiner = new StringJoiner(" ");
//        if(!CollectionUtils.isEmpty(user.getRoles())){
//            user.getRoles().forEach(role -> {
//                stringJoiner.add("ROLE_" + role.getName());
//                if(!CollectionUtils.isEmpty(role.getPermissions()))
//                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()) );
//            });
//        }
//        return stringJoiner.toString();
//    }
}

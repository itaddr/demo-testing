package com.itaddr.demo.testing;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.itaddr.common.tools.enums.KeysEnum;
import com.itaddr.common.tools.utils.ByteUtil;
import com.itaddr.common.tools.utils.CodecUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

public class JWTTest {

    private static String HMAC256_KEY = "abcdefghijklmnop";


    private static Algorithm ALGORITHM = Algorithm.HMAC256(HMAC256_KEY);
    private static JWT JWT_CONS = new JWT();


    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @Test
    public void javaJwtTest() throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyPair keyPair = CodecUtil.genKeyPair(KeysEnum.RSA2048);
        RSAPublicKey aPub = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey aPri = (RSAPrivateKey) keyPair.getPrivate();

        Algorithm signature = Algorithm.RSA256(null, aPri);
        Algorithm verification = Algorithm.RSA256(aPub, null);

        long currentTimeMillis = System.currentTimeMillis();

        JWTCreator.Builder builder = JWT.create();
        builder.withJWTId(UUID.randomUUID().toString()) // jwt的唯一身份标识，主要用来作为一次性token,从而回避重放攻击。
                .withIssuer("issuer") // jwt签发者
                .withSubject("subject") // jwt所面向的用户
                .withAudience("audience") // 接收jwt的一方
                .withIssuedAt(new Date(currentTimeMillis)) // jwt的签发时间
                .withNotBefore(new Date(currentTimeMillis)) // 定义在什么时间之前，该jwt都是不可用的.
                .withExpiresAt(new Date(currentTimeMillis + 2 * 60 * 60 * 1000L)); // jwt的过期时间，这个过期时间必须要大于签发时间
        String token = builder.sign(signature);
        System.out.printf("token=%s\n", token);

        DecodedJWT decode;
        try {
            decode = JWT_CONS.decodeJwt(token);
            System.out.println("Token解析成功");
        } catch (JWTDecodeException jde) {
            System.out.println("Token解析失败");
            throw jde;
        }
        try {
            verification.verify(decode);
            System.out.println("Token验签成功");
        } catch (SignatureVerificationException sve) {
            System.out.println("Token验签失败");
            throw sve;
        }

        System.out.printf("header=%s\n", new String(Base64.getDecoder().decode(decode.getHeader()), StandardCharsets.US_ASCII));
        System.out.printf("payload=%s\n", new String(Base64.getDecoder().decode(decode.getPayload()), StandardCharsets.US_ASCII));
        System.out.printf("signature=%s\n", ByteUtil.toLowerHexString(Base64.getUrlDecoder().decode(decode.getSignature())));
    }

    @Test
    public void jjwtTest() {
        long currentTimeMillis = System.currentTimeMillis();

        Map<String, Object> claims = new HashMap<>(); //创建payload的私有声明（根据特定的业务需要添加，如果要拿这个做验证，一般是需要和jwt的接收方提前沟通好验证方式的）
        claims.put("uid", "DSSFAWDWADAS...");
        claims.put("user_name", "admin");
        claims.put("nick_name", "DASDA121");

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setId(UUID.randomUUID().toString())
                .setIssuer("issuer")
                .setSubject("subject")
                .setAudience("audience")
                .setIssuedAt(new Date(currentTimeMillis))
                .setNotBefore(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + 2 * 60 * 60 * 1000L))
                .signWith(signatureAlgorithm, HMAC256_KEY);
        String token = builder.compact();
        System.out.printf("token=%s\n", token);

        JwtParser parser = Jwts.parser().setSigningKey(HMAC256_KEY);

        Jwt<Header, DefaultClaims> parse = parser.parse(token);
        System.out.printf("header=%s\n", parse.getHeader());
        System.out.printf("payload=%s\n", parse.getBody());
    }

}

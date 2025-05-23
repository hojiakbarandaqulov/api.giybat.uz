package api.giybat.uz.util;

import api.giybat.uz.dto.JwtDTO;
import api.giybat.uz.enums.ProfileRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

public class JwtUtil {
    private static final int tokenLiveTime = 1000 * 3600 * 96; // 2-day
    private static final String secretKey = "verylongmazgiskjdhskjdhadasdasgfgdfgdfdftrhdgrgefergetdgsfegvergdgsbdzsfbvgdsetbgrFLKWRMFKJERNGVSFUOISNIUVNSDBFIUSHIULFHWAUOIESIUOFIOEJOIGJMKLDFMGghjgjOTFIJBP";

    public static String encode(Integer profileId, String email) {
        return Jwts
                .builder()
                .subject(email)
                .subject(String.valueOf(profileId))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (tokenLiveTime)))
                .signWith(getSignInKey())
                .compact();
    }
    public static String encode(String username, Integer profileId, List<ProfileRole> roleList) {
        String strRoles = roleList.stream().map(Enum::name).
                collect(Collectors.joining(","));

        Map<String, String> claims = new HashMap<>();
        claims.put("roles", strRoles);
        claims.put("id", String.valueOf(profileId));

        return Jwts
                .builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (tokenLiveTime)))
                .signWith(getSignInKey())
                .compact();
    }

    public static JwtDTO decode(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String username = claims.getSubject();
        Integer id = Integer.valueOf((String) claims.get("id"));
        String strRoles = (String) claims.get("roles");
        List<ProfileRole> roleLis = Arrays.stream(strRoles.split(","))
                .map(ProfileRole::valueOf)
                .toList();
        return new JwtDTO(id, username, roleLis);
    }

    public static Integer decodeVerRegToken(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Integer.valueOf(claims.getSubject());
    }

    private static SecretKey getSignInKey() {
        byte[] keyBytes = Base64.getUrlDecoder().decode(secretKey);
        return  Keys.hmacShaKeyFor(keyBytes);
    }
}



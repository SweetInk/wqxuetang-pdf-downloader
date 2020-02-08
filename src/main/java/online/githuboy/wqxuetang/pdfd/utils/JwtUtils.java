package online.githuboy.wqxuetang.pdfd.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultHeader;
import online.githuboy.wqxuetang.pdfd.Constants;

/**
 * JWT签名工具
 *
 * @author suchu
 * @since 2020年2月3日
 */
public class JwtUtils {

    private JwtUtils() {
    }

    /**
     * 生成用于请求书籍图片的JWT string
     * 参数逆向至js源码
     * * b: "2175744"
     * * iat: 1580656062
     * * k: "{"u":"R1NInpLknfY=","i":"060boAYqHrTaynN+ECR+3g==","t":"exSEISn89lV1tiRwOvhXwQ==","b":"yvh75KLIVvU=","n":"LbcQPI6S3A0="}"
     * * p: 4
     * * t: 1580656021000
     * * w: 1000
     * * -------------------------------------------------------------------
     * * b: "2175744"
     * * iat: 1580656062
     * * k: "{"u":"R1NInpLknfY=","i":"060boAYqHrTaynN+ECR+3g==","t":"exSEISn89lV1tiRwOvhXwQ==","b":"yvh75KLIVvU=","n":"LbcQPI6S3A0="}"
     * * p: 4
     * * t: 1580656021000
     * * w: 1000
     *
     * @param bookId     书籍id
     * @param pageNumber 页码
     * @param k          书籍图片验证Key
     * @return JwtTokenString
     */
    public static String getJwt(String bookId, String pageNumber, String k) {
        return Jwts.builder()
                .setHeaderParam(DefaultHeader.TYPE, DefaultHeader.JWT_TYPE)
                .claim("p", pageNumber)
                .claim("t", System.currentTimeMillis())
                .claim("b", bookId)
                .claim("w", 1e3)
                .claim("k", k)
                .claim("iat", System.currentTimeMillis() / 1000)
//                .signWith(new SecretKeySpec(Constants.JWT_SECRET.getBytes(),SignatureAlgorithm.HS256.getJcaName()))
                .signWith(SignatureAlgorithm.HS256, Constants.JWT_SECRET.getBytes())
                .compact();
    }

    public static String test() {
        String testK = "{\"u\":\"R1NInpLknfY=\",\"i\":\"060boAYqHrTaynN+ECR+3g==\",\"t\":\"exSEISn89lV1tiRwOvhXwQ==\",\"b\":\"yvh75KLIVvU=\",\"n\":\"LbcQPI6S3A0=\"}";
        return Jwts.builder()
                .setHeaderParam(DefaultHeader.TYPE, DefaultHeader.JWT_TYPE)
                .claim("p", "4")
                .claim("t", "1580656021000")
                .claim("b", "2175744")
                .claim("w", "1000")
                .claim("k", testK)
                .claim("iat", "1580656062")
                .signWith(SignatureAlgorithm.HS256, Constants.JWT_SECRET.getBytes())
                .compact();
    }
}

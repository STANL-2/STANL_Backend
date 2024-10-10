package stanl_2.weshareyou.global.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import stanl_2.weshareyou.global.common.exception.CommonException;
import stanl_2.weshareyou.global.common.exception.ErrorCode;
import stanl_2.weshareyou.global.security.constants.ApplicationConstants;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Slf4j
public class TokenFilter extends OncePerRequestFilter {
    private final ApplicationConstants applicationConstants;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String token = request.getHeader("Authorization");

        if(token == null || token.isEmpty() || !token.startsWith("Bearer ")){
            throw new CommonException(ErrorCode.NOT_FOUND_JWT_TOKEN);
        }else {
            String jwtToken = token.substring(7);

            // 시크릿 키로 JWT를 파싱
            SecretKey secretKey = Keys.hmacShaKeyFor(applicationConstants.getJWT_SECRET_DEFAULT_VALUE().getBytes(StandardCharsets.UTF_8));

            // JWT 토큰에서 클레임을 추출
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();

            log.info("1");
            log.info("id값 : {}", claims.getId());
            // jwt 토큰에서 추출
            long id = claims.get("id", Long.class);
            String loginId = claims.get("loginId", String.class);
            String nationality = claims.get("nationality", String.class);
            String sex = claims.get("sex", String.class);
            int point = claims.get("point", Integer.class);
            String nickname = claims.get("nickname", String.class);
//            String profile = claims.get("profile", String.class);
//            String introduction = claims.get("introduction", String.class);
            String language = claims.get("language", String.class);

            // request에 넣기
            request.setAttribute("id", id);
            request.setAttribute("loginId", loginId);
            request.setAttribute("nationality", nationality);
            request.setAttribute("sex", sex);
            request.setAttribute("point", point);
            request.setAttribute("nickname", nickname);
//            request.setAttribute("profile", profile);
//            request.setAttribute("introduction", introduction);
            request.setAttribute("language", language);
            request.setAttribute("Authorization", token);
        }



        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getServletPath().equals("/api/v1/member/login");
    }

}
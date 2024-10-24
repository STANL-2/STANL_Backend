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
            // 보안 X
            filterChain.doFilter(request, response);  // 토큰이 없으면 필터를 그냥 통과시킴

            // 보안 O
//            throw new CommonException(ErrorCode.NOT_FOUND_JWT_TOKEN);
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

            // jwt 토큰에서 추출
            Long id = claims.get("id", Long.class);
            String loginId = claims.get("loginId", String.class);
            String nationality = claims.get("nationality", String.class);
            String sex = claims.get("sex", String.class);
            Integer point = claims.get("point", Integer.class);
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
        String path = request.getServletPath();
        return path.equals("/api/v1/member/login") ||
                path.equals("/api/v1/member/register") ||
                path.startsWith("/api/v1/member/sms") ||  // 와일드카드 경로 포함
                path.startsWith("/api/v1/member/mail") ||
                path.equals("/api/v1/member") ||
                path.equals("/api/v1/member/password") ||
                path.startsWith("/ws");
    }

}

package stanl_2.weshareyou.global.security.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;
import stanl_2.weshareyou.global.security.constants.ApplicationConstants;

@RequiredArgsConstructor
public class JWTTokenGeneratorFilter extends OncePerRequestFilter {


    ApplicationConstants applicationConstants;

    /**
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request
            , HttpServletResponse response
            , FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null != authentication) {
            Environment env = getEnvironment(); // 환경변수
            if (null != env) {

                String secret = env.getProperty(applicationConstants.getJWT_SECRET_KEY(),
                        applicationConstants.getJWT_SECRET_DEFAULT_VALUE());
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                String jwt = Jwts.builder().setIssuer("STANL2").setSubject("JWT Token")
                        .claim("username", authentication.getName())
                        .claim("authorities", authentication.getAuthorities().stream().map(
                                GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                        .setIssuedAt(new Date())
                        .setExpiration(new Date((new Date()).getTime() + 30000000)) // 만료시간 8시간
                        .signWith(secretKey).compact(); // Digital Signature 생성
                response.setHeader(applicationConstants.getJWT_HEADER(), jwt);
            }
        }
        filterChain.doFilter(request, response);
    }
}
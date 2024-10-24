package stanl_2.weshareyou.global.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import stanl_2.weshareyou.global.security.constants.ApplicationConstants;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final ApplicationConstants applicationConstants;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() == StompCommand.CONNECT) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token == null || token.isEmpty() || !token.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Invalid token");
            } else {
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

                // WebSocket session에 넣거나 사용자 검증 로직 수행
                accessor.setHeader("id", id);
                accessor.setHeader("loginId", loginId);
                accessor.setHeader("nationality", nationality);
                accessor.setHeader("sex", sex);
                accessor.setHeader("point", point);
                accessor.setHeader("nickname", nickname);

                log.info("거쳐갔습니다 ");
            }
        }
        return message;
    }


}

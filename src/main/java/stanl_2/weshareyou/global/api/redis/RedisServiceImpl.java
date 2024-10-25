package stanl_2.weshareyou.global.api.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service(value = "RedisService")
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate template;

    /* key-value 값 가져오는 메소드 */
    public String getData(String key) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        return valueOperations.get(key);
    }

    /* Key에 해당하는 value가 존재하는지 확인 */
    public boolean isExistData(String key) {
        return Boolean.TRUE.equals(template.hasKey(key));
    }

    /* key-value 저장하는 메소드(만료시간까지) */
    public void setDataExpire(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

    /* key에 해당하는 데이터 지우기 */
    public void deleteData(String key) {
        template.delete(key);
    }
}

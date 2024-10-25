package stanl_2.weshareyou.global.api.redis;

public interface RedisService {
    /* key-value 값 가져오는 메소드 */
    String getData(String key);

    /* Key에 해당하는 value가 존재하는지 확인 */
    boolean isExistData(String key);

    /* key-value 저장하는 메소드(만료시간까지) */
    void setDataExpire(String key, String value, long duration);

    /* key에 해당하는 데이터 지우기 */
    void deleteData(String key);
}

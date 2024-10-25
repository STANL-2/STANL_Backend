package stanl_2.weshareyou.global.api.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import stanl_2.weshareyou.global.api.redis.RedisService;

@Slf4j
@Service(value = "SmsService")
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService{
    private final RedisService redisService;

    public Boolean verifySmsCode(String phone, String code) {
        String codeFoundBySms = redisService.getData(phone);

        if (codeFoundBySms == null) {
            return false;
        }

        return codeFoundBySms.equals(code);
    }


    @Override
    public void deleteKey(String phone) {
        redisService.deleteData(phone);
    }
}

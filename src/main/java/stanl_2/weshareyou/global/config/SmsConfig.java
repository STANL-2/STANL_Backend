package stanl_2.weshareyou.global.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import stanl_2.weshareyou.global.common.dto.SmsDTO;
import stanl_2.weshareyou.global.common.exception.CommonException;
import stanl_2.weshareyou.global.common.exception.ErrorCode;
import stanl_2.weshareyou.global.api.redis.RedisService;

import java.security.SecureRandom;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SmsConfig {

    @Value("${spring.sms.api-key}")
    private String apiKey;

    @Value("${spring.sms.api-secret}")
    private String apiSecret;

    @Value("${spring.sms.provider}")
    private String smsProvider;

    @Value("${spring.sms.sender}")
    private String smsSender;

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final RedisService redisService;
    private DefaultMessageService messageService;

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(
                this.apiKey,
                this.apiSecret,
                this.smsProvider
        );
    }

    private String createCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

//    @Transactional
    public void sendSms(SmsDTO requestSendSms) {
        String authCode = createCode();

        Message message = new Message(); // 생성자를 통해 API 키와 API 시크릿 전달

        // 발신 번호
        message.setFrom(smsSender);
        // 수신 번호
        message.setTo(requestSendSms.getPhone());
        // 메시지 내용
        message.setText(
                "[SMS] STANL2 인증번호" +
                        "\n" +
                "[인증번호] " + authCode

        );

        // redis에 해당 번호의 인증번호가 있으면 삭제
        if(redisService.isExistData(requestSendSms.getPhone())) {
            redisService.deleteData(requestSendSms.getPhone());
        }

        // redis에 인증번호, 이메일 저장
        redisService.setDataExpire(requestSendSms.getPhone(), authCode, 60 * 5L);

        // 초대장 전송
        SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));

        if(response == null){
            throw new CommonException(ErrorCode.SMS_SEND_FAILURE);
        }
    }
}

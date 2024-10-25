package stanl_2.weshareyou.global.api.smtp;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import stanl_2.weshareyou.global.api.redis.RedisService;

import java.security.SecureRandom;

@Slf4j
@Service(value = "MailService")
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final RedisService redisService;
    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    @Value("${spring.mail.host.port.username}")
    private String configEmail;

    /* 6자리 난수 생성 */
    private String createCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    /* Thymeleaf 기반의 html 파일에 값 넣고 연결하는 메소드 */
    private String setContext(String code) {
        // thymeleaf 기반의 html 파일에 값을 넣고 연결
        Context context = new Context();
        // templateengine과 classloadertemplateresolver를 활용하여 resource/template에 위치한 mail.html 연결
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

        // 매개변수 저장
        context.setVariable("code", code);

        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);

        templateEngine.setTemplateResolver(templateResolver);

        return templateEngine.process("mail", context);
    }

    private MimeMessage createEmailForm(String email) throws MessagingException {
        String authCode = createCode();

        // MimeMessage에 코드, 송신 이메일, 내용 보관
        MimeMessage message = mailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("STANL2 본인 확인");
        message.setFrom(configEmail);
        message.setText(setContext(authCode), "utf-8", "html");

        // redis에 인증번호, 이메일 저장
        redisService.setDataExpire(email, authCode, 60 * 5L);

        return message;
    }

    /* 만든 메일 전송 */
    public void sendEmail(String toEmail) throws MessagingException {
        // redis에 해당 이메일이 있으면 db에서 삭제
        if(redisService.isExistData(toEmail)) {
            redisService.deleteData(toEmail);
        }

        MimeMessage emailForm = createEmailForm(toEmail);
        mailSender.send(emailForm);
    }


    // 보낸 이메일과 인증번호 일치하는지 확인
    public Boolean verifyEmailCode(String email, String code) {
        String codeFoundByEmail = redisService.getData(email);
        if (codeFoundByEmail == null) {
            return false;
        }
        return codeFoundByEmail.equals(code);
    }

}

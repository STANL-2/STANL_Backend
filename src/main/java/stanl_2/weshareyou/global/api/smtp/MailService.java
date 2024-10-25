package stanl_2.weshareyou.global.api.smtp;


import jakarta.mail.MessagingException;

public interface MailService {

    /* 만든 메일 전송 */
    void sendEmail(String toEmail) throws MessagingException;


    // 보낸 이메일과 인증번호 일치하는지 확인
    Boolean verifyEmailCode(String email, String code);
}

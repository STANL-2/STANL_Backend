package stanl_2.weshareyou.global.api.sms;

public interface SmsService {

    Boolean verifySmsCode(String phone, String code);

    void deleteKey(String phone);
}

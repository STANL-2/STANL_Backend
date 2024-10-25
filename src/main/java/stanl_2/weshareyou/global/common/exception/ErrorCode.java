package stanl_2.weshareyou.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /**
     * 400(Bad Request)
     *   이 응답은 잘못된 문법으로 인하여 서버가 요청을 이해할 수 없음을 의미합니다.
     * */
    WRONG_ENTRY_POINT(40000, HttpStatus.BAD_REQUEST, "잘못된 접근입니다."),
    MISSING_REQUEST_PARAMETER(40001, HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),
    INVALID_PARAMETER_FORMAT(40002, HttpStatus.BAD_REQUEST, "요청에 유효하지 않은 인자 형식입니다."),
    BAD_REQUEST_JSON(40003, HttpStatus.BAD_REQUEST, "잘못된 JSON 형식입니다."),
    // 데이터 무결성 위반 추가(ex: db의 NOT NULL 속성)
    DATA_INTEGRITY_VIOLATION(40005, HttpStatus.BAD_REQUEST,
            "데이터 무결성 위반입니다. 필수 값이 누락되었거나 유효하지 않습니다."),
    REGISTER_FAIL(40006, HttpStatus.BAD_REQUEST, "회원 가입 실패!"),
    NOTICE_REGISTER_FAIL(40007, HttpStatus.BAD_REQUEST, "공지 생성 실패!"),
    NOTICE_UPDATE_FAIL(40008, HttpStatus.BAD_REQUEST, "공지 수정 실패!"),
    NOTICE_DELETE_FAIL(40009, HttpStatus.BAD_REQUEST, "공지 삭제 실패!"),
    PRODUCT_IS_RENTAL(40010, HttpStatus.BAD_REQUEST, "이미 대여된 물품입니다."),
    PRODUCT_IS_RETURN(40011, HttpStatus.BAD_REQUEST, "이미 반납된 물품입니다."),
    PRODUCT_IS_NOT_RETURN(40012, HttpStatus.BAD_REQUEST, "물품 반납에 실패하였습니다."),
    VALIDATION_FAIL(40013, HttpStatus.BAD_REQUEST, "유효성 검사 실패"),
    SMS_SEND_FAILURE(40014, HttpStatus.BAD_REQUEST, "문자 전송 실패"),
    BAD_REQUEST_IMAGE(40015, HttpStatus.BAD_REQUEST, "잘못된 Image 형식입니다."),


    /**
     * 401(Unauthorized)
     *   비록 HTTP 표준에서는 "미승인(unauthorized)"를 명확히 하고 있지만,
     *   의미상 이 응답은 "비인증(unauthenticated)"을 의미합니다.
     *   클라이언트는 요청한 응답을 받기 위해서는 반드시 스스로를 인증해야 합니다.
     * */
    LOGIN_FAILURE(40100, HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다"),
    INVALID_TOKEN_ERROR(40101, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EMAIL_VERIFY_FAIL(40102, HttpStatus.UNAUTHORIZED, "이메일 인증 실패하셨습니다."),
    SMS_VERIFY_FAIL(40203, HttpStatus.UNAUTHORIZED, "SMS 인증 실패하셨습니다."),

    /**
     * 403(Forbidden)
     * 클라이언트는 콘텐츠에 접근할 권리를 가지고 있지 않습니다.
     * 예를들어 그들은 미승인이어서 서버는 거절을 위한 적절한 응답을 보냅니다. 401과 다른 점은 서버가 클라이언트가 누구인지 알고 있습니다.
     */
    FORBIDDEN_ROLE(40300, HttpStatus.FORBIDDEN, "권한이 존재하지 않습니다."),


    /**

     * 404(Not Found)
     *   서버는 요청받은 리소스를 찾을 수 없습니다. 브라우저에서는 알려지지 않은 URL을 의미합니다.
     *   이것은 API에서 종점은 적절하지만 리소스 자체는 존재하지 않음을 의미할 수도 있습니다.
     *   서버들은 인증받지 않은 클라이언트로부터 리소스를 숨기기 위하여 이 응답을 403 대신에 전송할 수도 있습니다.
     *   이 응답 코드는 웹에서 반복적으로 발생하기 때문에 가장 유명할지도 모릅니다.
     * */
    USERDETAILS_NOT_FOUND(40400, HttpStatus.NOT_FOUND, "User Details를 찾을 수 없습니다."),
    ALREADY_LIKED(40401, HttpStatus.BAD_REQUEST, "이미 좋아요한 게시글입니다."),
    BOARD_NOT_FOUND(40402, HttpStatus.BAD_REQUEST, "해당게시글을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(40403, HttpStatus.BAD_REQUEST, "해당 멤버을 찾을 수 없습니다."),
    NO_LIKES_FOUND(40404, HttpStatus.BAD_REQUEST, "해당 멤버의 좋아요를 찾을 수 없습니다."),
    NOTICE_NOT_FOUND(40405, HttpStatus.BAD_REQUEST, "해당 공지를 찾을 수 없습니다."),
    NOT_FOUND_LIKE(40406, HttpStatus.BAD_REQUEST, "좋아요를 누르지 않았습니다."),
    PRODUCT_NOT_FOUND(40407, HttpStatus.NOT_FOUND, "해당 공유물품 게시글을 찾을 수 없습니다."),
    PRODUCT_AUTHOR_NOT_FOUND(40408, HttpStatus.NOT_FOUND, "해당 공유물품 게시글의 작성자가 아닙니다."),
    NOT_FOUND_CSRF_TOKEN(40409, HttpStatus.NOT_FOUND, "CSRF 토큰이 요청에 존재하지 않습니다."),
    NOT_FOUND_JWT_TOKEN(40410, HttpStatus.NOT_FOUND, "JWT 토큰이 없습니다."),
    COMMENT_NOT_FOUND(40411, HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
    ALARM_NOT_FOUND(40412, HttpStatus.NOT_FOUND, "해당 알림을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(40413, HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),
  
    /**
     * 500(Internal Server Error)
     *   서버가 처리 방법을 모르는 상황이 발생했습니다. 서버는 아직 처리 방법을 알 수 없습니다.
     * */
    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");


    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}

package stanl_2.weshareyou.domain.member.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import stanl_2.weshareyou.global.common.RequestUtils;
import stanl_2.weshareyou.domain.member.aggregate.dto.MemberDTO;
import stanl_2.weshareyou.domain.member.aggregate.history.HistoryInput;
import stanl_2.weshareyou.domain.member.aggregate.vo.request.*;
import stanl_2.weshareyou.domain.member.aggregate.vo.response.*;
import stanl_2.weshareyou.domain.member.aggregate.vo.response.findlikeboard.FindLikeListResponseVO;
import stanl_2.weshareyou.domain.member.aggregate.vo.response.findmyboard.FindMypageListResponseVO;
import stanl_2.weshareyou.domain.member.aggregate.vo.response.findmycomment.FindMyCommentListResponseVO;
import stanl_2.weshareyou.domain.member.service.MemberService;
import stanl_2.weshareyou.global.common.dto.SmsDTO;
import stanl_2.weshareyou.global.common.exception.CommonException;
import stanl_2.weshareyou.global.common.exception.ErrorCode;
import stanl_2.weshareyou.global.common.response.ApiResponse;
import stanl_2.weshareyou.global.config.SmsConfig;
import stanl_2.weshareyou.global.security.service.sms.SmsService;
import stanl_2.weshareyou.global.security.service.smtp.MailService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController("value = memberController")
@RequestMapping("/api/v1/member")
public class MemberController {

    private final MemberService memberService;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;
    private final SmsConfig smsConfig;
    private final SmsService smsService;

    /* 설명. jwt토큰 활용 샘플 예시 코드 */
    @GetMapping("/health")
    public ApiResponse<?> healthCheck(@RequestAttribute("id") Long id,
                                      @RequestAttribute("loginId") String loginId,
                                      @RequestAttribute("nationality") String nationality,
                                      @RequestAttribute("sex") String sex,
                                      @RequestAttribute("point") Integer point,
                                      @RequestAttribute("nickname") String nickname,
                                      @RequestAttribute("language") String language) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("loginId", loginId);
        result.put("nationality", nationality);
        result.put("sex", sex);
        result.put("point", point);
        result.put("nickname", nickname);
        result.put("language", language);

        return ApiResponse.ok(result);
    }

    /**
     * 내용 : 회원가입
     * URL: [POST] localhost:8080/api/v1/member/register
     * Request body
     * {
     *     "loginId": "test@gmail.com",
     *     "password": "test",
     *     "name": "user1",
     *     "age": 21,
     *     "nationality": "seoul",
     *     "sex": "FEMALE",
     *     "phone": "01012345678",
     *     "role": "ROLE_MEMBER",
     *     "nickname": "가지남",
     *     "language": "KOREAN"
     * }
     * */
    @PostMapping("/register")
    public ApiResponse<?> registerMember(@RequestBody @Valid RegisterRequestVO memberInfo) {
        MemberDTO memberRequestDTO = modelMapper.map(memberInfo, MemberDTO.class);
        MemberDTO memberResponseDTO = memberService.registMember(memberRequestDTO);

        if(memberResponseDTO.getId() <= 0) {
            throw new CommonException(ErrorCode.REGISTER_FAIL);
        }

        RegisterResponseVO registerResponseVO = modelMapper.map(memberResponseDTO, RegisterResponseVO.class);

        return ApiResponse.ok(registerResponseVO);
    }

    @RequestMapping("/userDetail")
    public MemberDTO getUserDetailsAfterLogin(Authentication authentication){
        Optional<MemberDTO> memberDetailDTO = memberService.findMemberDetail(authentication.getName());

        return memberDetailDTO.orElse(null);
    }

    /**
     * 내용 : 로그인
     * URL: [Post] localhost:8080/api/v1/member/login
     * Request body
     * {
     *     "loginId": "test@gmail.com",
     *     "password": "test"
     * }
     */
    @PostMapping("/login")
    public ApiResponse<?> loginMember(@RequestBody @Valid LoginRequestVO loginRequestVO, HttpServletRequest request) {
        Authentication authentication = UsernamePasswordAuthenticationToken
                .unauthenticated(loginRequestVO.getLoginId(), loginRequestVO.getPassword());

        Authentication authenticationResponse = authenticationManager.authenticate(authentication);

        String jwt = memberService.loginMember(authenticationResponse);
        if ("".equals(jwt)) {
            throw new CommonException(ErrorCode.LOGIN_FAILURE);
        }

        // 로그인 이력 저장
        HistoryInput historyInput = new HistoryInput();
        historyInput.setUserId(loginRequestVO.getLoginId());
        historyInput.setClientIp(RequestUtils.getClientIp(request));
        historyInput.setUserAgent(request.getHeader("User-Agent"));
        memberService.saveLoginHistory(historyInput);

        return ApiResponse.ok(new LoginResponseVO(HttpStatus.OK.getReasonPhrase(), jwt));
    }


    /**
     * 내용 : 인증번호 이메일 전송
     * URL: [POST] localhost:8080/api/v1/member/mail
     *
     * JWT 토큰만 있으면 된다.
     */
    @PostMapping("/mail")
    public ApiResponse<?> sendEmailCheck(@RequestBody SendEmailRequestVO sendEmailRequestVO) throws MessagingException {
        mailService.sendEmail(sendEmailRequestVO.getEmail());
        return ApiResponse.ok("이메일 전송 성공!");
    }

    /**
     * 내용 : 인증번호 확인
     * URL: [GET] localhost:8080/api/v1/member/check
     *
     * JWT Token, 인증번호(Request Body)
     */
    @PostMapping("/mail/check")
    public ApiResponse<?> checkEmailCode(@RequestBody CheckEmailCodeRequestVO checkEmailCodeRequestVO){
        if(!mailService.verifyEmailCode(checkEmailCodeRequestVO.getEmail(), checkEmailCodeRequestVO.getCode())) {
            throw new CommonException(ErrorCode.EMAIL_VERIFY_FAIL);
        }
        return ApiResponse.ok("이메일 인증 성공!");
    }


    /**
     * 내용 : 회원탈퇴(회원 비활성화)
     * [DELETE] localhost:8080/api/v1/member
     * JWT 토큰의 pk 값으로 회원 비활성화
     * member_active -> false(0)
     */
    @DeleteMapping("")
    public ApiResponse<?> resign(@RequestAttribute("id") Long id){

        memberService.deleteMember(id);

        return ApiResponse.ok("회원 탈퇴에 성공했습니다.");
    }

    /**
     * 내용: 비밀번호 재설정
     * [PUT] localhost:8080/api/v1/member/password
     * JWT 토큰의 pk 값으로 회원 비밀번호 재설정
     * Request Body
     * {
     *     "password": "abc"
     * }
     */
    @PutMapping("/password")
    public ApiResponse<?> updatePwd(@RequestBody UpdatePwdRequestVO updatePwdRequestVO){
        MemberDTO memberRequestDTO = modelMapper.map(updatePwdRequestVO, MemberDTO.class);

        memberService.updatePwd(memberRequestDTO);

        return ApiResponse.ok("비밀번호 재설정 성공!");
    }

    /**
     * 내용: 다른 사람 없이
     * Response
     * nickname
     */
    @GetMapping("/otherprofile")
    public ApiResponse<?> findOtherProfile(@RequestParam String nickname){
        MemberDTO responseMemberDTO = memberService.findOtherProfile(nickname);

        return ApiResponse.ok(responseMemberDTO);
    }

    /**
     * 내용: 회원 프로필 수정
     * [PUT] localhost:8080/api/v1/member/profile
     * JWT 토큰의 pk 값과 Request Body를 활용한 프로필 수정
     * request
     * {
     *     "nickname": "나자나",
     *     "profile_url": "http://www.gaodls.com",
     *     "introduction": "안뇽!",
     *     "language": "Deutsch"
     * }
     *
     * response
     * {
     *      "nickname": "나자나",
     *      "profile_url": null,
     *      "introduction": "안뇽!",
     *      "language": "Deutsch"
     *}
     */
    @PutMapping("/profile")
    public ApiResponse<?> updateProfile(@RequestAttribute("id") Long id,
                                        @RequestPart("vo") UpdateProfileRequestVO updateProfileRequestVO,
                                        @RequestPart(value = "file", required = false) MultipartFile profileImage) {

        MemberDTO requestMemberDTO = modelMapper.map(updateProfileRequestVO, MemberDTO.class);
        requestMemberDTO.setId(id);

        MemberDTO responseMemberDTO = memberService.updateProfile(requestMemberDTO, profileImage);

        UpdateProfileResponseVO updateProfileResponseVO = modelMapper.map(responseMemberDTO, UpdateProfileResponseVO.class);

        return ApiResponse.ok(updateProfileResponseVO);
    }


    /**
     * 내용: 마이페이지 수정
     * [PUT] localhost:8080/api/v1/member/mypage
     * JWT 토큰의 pk 값과 Request Body를 활용한 프로필 수정
     * Request
     * {
     *     "phone": "01038482048"
     * }
     *
     * Response
     * {
     *      "loginId": "bangdh1593@gmail.com",
     *      "name": "기기지",
     *      "age": 21,
     *      "sex": "FEMALE",
     *      "phone": "01038482048",
     *      "point": 0,
     *      "role": "ROLE_MEMBER",
     *      "createdAt": "2024-10-10T20:26:05",
     *      "updatedAt": "2024-10-11T00:16:46",
     *      "nationality": "seoul"
     * }
     */
    @PutMapping("/mypage")
    public ApiResponse<?> updateMypage(@RequestAttribute("id") Long id,
                                       @RequestBody UpdateMypageRequestVO updateMypageRequestVO){

        MemberDTO requestMemberDTO = modelMapper.map(updateMypageRequestVO, MemberDTO.class);
        requestMemberDTO.setId(id);
        MemberDTO responseMemberDTO  = memberService.updateMypage(requestMemberDTO);

        UpdateMypageResponseVO updateMypageResponseVO = modelMapper.map(responseMemberDTO, UpdateMypageResponseVO.class);

        return ApiResponse.ok(updateMypageResponseVO);
    }

    /**
     * 내용 : 포인트 적립
     * [PUT] localhost:8080/api/v1/member/mypage
     * JWT 토큰의 pk 값과 Request Body를 활용한 포인트 적립
     * Request
     * {
     *     "point": 10
     * }
     * Response
     * {
     *      "point": 10
     * }
     */
    @PutMapping("/point")
    public ApiResponse<?> earnPoint(@RequestAttribute("id") Long id,
                                    @RequestBody EarnPointRequestVO earnPointRequestVO){
        MemberDTO requestMemberDTO = modelMapper.map(earnPointRequestVO, MemberDTO.class);
        requestMemberDTO.setId(id);

        MemberDTO responseMemberDTO = memberService.earnPoint(requestMemberDTO);

        EarnPointResponseVO earnPointResponseVO = modelMapper.map(responseMemberDTO, EarnPointResponseVO.class);

        return ApiResponse.ok(earnPointResponseVO);
    }

    /**
     * 내용: 아이디 찾기를 위한 휴대전화 전송
     *
     */
    @PostMapping("/sms")
    public ApiResponse<?> sendSmsCheck(@RequestBody SendSmsRequestVO sendSmsRequestVO){

        MemberDTO requestMemberDTO = new MemberDTO();
        requestMemberDTO.setName(sendSmsRequestVO.getName());
        requestMemberDTO.setPhone(sendSmsRequestVO.getPhone());

        MemberDTO responseMemberDTO = memberService.checkMember(requestMemberDTO);

        SmsDTO requestSms = modelMapper.map(responseMemberDTO, SmsDTO.class);

        smsConfig.sendSms(requestSms);

        return ApiResponse.ok("인증번호 전송 성공!");
    }

    /**
     * 내용: sms인증에 성공하셨습니다!
     *
     */
    @PostMapping("/sms/check")
    public ApiResponse<?> checkSmsCode(@RequestBody CheckSmsCodeRequestVO checkSmsCodeRequestVO){

        if(!smsService.verifySmsCode(checkSmsCodeRequestVO.getPhone(), checkSmsCodeRequestVO.getCode())) {
            throw new CommonException(ErrorCode.SMS_VERIFY_FAIL);
        }else{
            return ApiResponse.ok("SMS 인증 성공!");
        }
    }

    /**
     * 내용 : 아이디 찾기
     * [GET] localhost:8080/api/v1/member
     * JWT 토큰의 pk 값을 활용한 아이디 찾기
     * Request
     * XXX
     * Response
     * {
     *      "loginId": "bangdh1593@gmail.com"
     * }
     */
    @GetMapping("")
    public ApiResponse<?> findId(@RequestAttribute("id") Long id){

        MemberDTO requestMemberDTO = new MemberDTO();
        requestMemberDTO.setId(id);

        MemberDTO responseMemberDTO = memberService.findId(requestMemberDTO);

        FindIdResponseVO findIdResponseVO = modelMapper.map(responseMemberDTO, FindIdResponseVO.class);

        return ApiResponse.ok(findIdResponseVO);
    }

    /**
     * 내용 : 프로필 조회
     */
    @GetMapping("/profile")
    public ApiResponse<?> findProfile(@RequestAttribute("id") Long id){
        MemberDTO requestMemberDTO = new MemberDTO();
        requestMemberDTO.setId(id);

        MemberDTO responseMemberDTO = memberService.findProfile(requestMemberDTO);

        FindProfileResponseVO findProfileResponseVO = modelMapper.map(responseMemberDTO, FindProfileResponseVO.class);

        return ApiResponse.ok(findProfileResponseVO);
    }

    /**
     * 내용 : 마이페이지 조회
     * [GET] localhost:8080/api/v1/member/mypage
     * JWT 토큰의 pk 값을 활용한 마이페이지 조회
     * Request
     * Response
     * {
     *      "loginId": "bangdh1593@gmail.com",
     *      "name": "기기지",
     *      "age": 21,
     *      "nationality": "seoul",
     *      "sex": "FEMALE",
     *      "phone": "01038482048",
     *      "point": 10,
     *      "role": "ROLE_MEMBER",
     *      "createdAt": "2024-10-10T20:26:05",
     *      "updatedAt": "2024-10-11T12:14:48"
     * }
     */
    @GetMapping("/mypage")
    public ApiResponse<?> findMypage(@RequestAttribute("id") Long id) {

        MemberDTO requestMemberDTO = new MemberDTO();
        requestMemberDTO.setId(id);

        MemberDTO responseMemberDTO = memberService.findMypage(requestMemberDTO);

        FindMypageResponseVO findMypageResponseVO = modelMapper.map(responseMemberDTO, FindMypageResponseVO.class);

        return ApiResponse.ok(findMypageResponseVO);
    }

    /**
     * 내용 : 포인트 조회
     * [GET] localhost:8080/api/v1/member/mypage
     * JWT 토큰의 pk 값을 활용한 포인트 조회
     * Request
     * Response
     * {
     *      "point": 10
     * }
     */
    @GetMapping("point")
    public ApiResponse<?> findPoint(@RequestAttribute("id") Long id) {

        MemberDTO requestMemberDTO = new MemberDTO();
        requestMemberDTO.setId(id);

        MemberDTO responseMemberDTO = memberService.findPoint(requestMemberDTO);

        FindPointResponseVO findPointRequestVO = modelMapper.map(responseMemberDTO, FindPointResponseVO.class);

        return ApiResponse.ok(findPointRequestVO);
    }

    /**
     * 내용 : 내가 쓴 글
     * [GET] localhost:8080/api/v1/member/myboard
     * JWT 토큰의 pk 값을 활용한 내가 쓴 글 조회
     * Request
     * Response
     * {
     *      "nickname": "가지남",
     *      "board": [
     *                      {
     *                              "title": "Guide to Paris",
     *                              "content": "A detailed guide to traveling in Paris.",
     *                              "commentCount": 0,
     *                              "likesCount": 0
     *                      },
     *                      {
     *                              "title": "Selling Camping Gear",
     *                              "content": "Selling my used camping gear at a good price.",
     *                              "commentCount": 0,
     *                              "likesCount": 0
     *                      },
     *                      {
     *                              "title": "Looking for Travel Companion",
     *                              "content": "Looking for a companion for a trip to Spain.",
     *                              "commentCount": 0,
     *                              "likesCount": 0
     *                      }
     *              ]
     *     }
     */
    @GetMapping("myboard")
    public ApiResponse<?> findMyBoard(@RequestAttribute("id") Long id) {

        MemberDTO requestMemberDTO = new MemberDTO();
        requestMemberDTO.setId(id);

        MemberDTO responseMemberDTO = memberService.findMyBoard(requestMemberDTO);

        FindMypageListResponseVO findMypageListResponseVO = modelMapper.map(responseMemberDTO, FindMypageListResponseVO.class);

        return ApiResponse.ok(findMypageListResponseVO);
    }

    /**
     * 내용 : 좋아요한 게시판 조회
     * [GET] localhost:8080/api/v1/member/likeboard
     * JWT 토큰의 pk 값을 활용한 좋아요한 게시판 조회
     * Request
     * Response
     * {
     *      "nickname": "가지남",
     *      "boardLike": [
     *                   {
     *                          "boardLikes": [
     *                                        {
     *                                              "title": "Guide to Paris",
     *                                              "content": "A detailed guide to traveling in Paris.",
     *                                              "commentCount": 0,
     *                                              "likesCount": 0
     *                                        }
     *                   ]
     *                   },
     *                   {
     *                          "boardLikes": [
     *                                        {
     *                                              "title": "Selling Camping Gear",
     *                                              "content": "Selling my used camping gear at a good price.",
     *                                              "commentCount": 0,
     *                                              "likesCount": 0
     *                                        }
     *                   ]
     *                   },
     *                   {
     *                          "boardLikes": [
     *                                        {
     *                                              "title": "Looking for Travel Companion",
     *                                              "content": "Looking for a companion for a trip to Spain.",
     *                                              "commentCount": 0,
     *                                              "likesCount": 1
     *                                        }
     *                   ]
     *                   }
     *                   ]
     *     }
     */
    @GetMapping("likeboard")
    public ApiResponse<?> findLikeBoard(@RequestAttribute("id") Long id) {
        MemberDTO requestMemberDTO = new MemberDTO();
        requestMemberDTO.setId(id);

        MemberDTO responseMemberDTO = memberService.findLikeBoard(requestMemberDTO);

        FindLikeListResponseVO findLikeListResponseVO = modelMapper.map(responseMemberDTO, FindLikeListResponseVO.class);

        return ApiResponse.ok(findLikeListResponseVO);
    }

    /**
     * 내용 : 내 댓글 조회
     * [GET] localhost:8080/api/v1/member/mycomment
     * JWT 토큰의 pk 값을 활용한 내 댓글 조회
     * Response
     * {
     *         "nickname": "가지남",
     *         "boardComment": [
     *             {
     *                 "content": "aaa",
     *                 "createdAt": "2024-10-13 00:05:23.0",
     *                 "updatedAt": "2024-10-13 00:05:23.896685"
     *             },
     *             {
     *                 "content": "bbb",
     *                 "createdAt": "2024-10-13 00:05:37.0",
     *                 "updatedAt": "2024-10-13 00:05:37.061557"
     *             },
     *             {
     *                 "content": "ccc",
     *                 "createdAt": "2024-10-13 00:05:40.0",
     *                 "updatedAt": "2024-10-13 00:05:40.415405"
     *             }
     *         ]
     *     }
     */
    @GetMapping("mycomment")
    public ApiResponse<?> findMyComment(@RequestAttribute("id") Long id) {
        MemberDTO requestMemberDTO = new MemberDTO();
        requestMemberDTO.setId(id);

        MemberDTO responseMemberDTO = memberService.findMyComment(requestMemberDTO);

        FindMyCommentListResponseVO findMyCommentResponseVO = modelMapper.map(responseMemberDTO, FindMyCommentListResponseVO.class);

        return ApiResponse.ok(findMyCommentResponseVO);
    }


}
package stanl_2.weshareyou.domain.member.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import stanl_2.weshareyou.domain.board.aggregate.entity.Board;
import stanl_2.weshareyou.domain.board_comment.aggregate.entity.BoardComment;
import stanl_2.weshareyou.domain.board_like.aggregate.entity.BoardLike;
import stanl_2.weshareyou.domain.member.aggregate.Role;
import stanl_2.weshareyou.domain.member.aggregate.dto.MemberDTO;
import stanl_2.weshareyou.domain.member.aggregate.entity.Member;
import stanl_2.weshareyou.domain.member.aggregate.history.HistoryInput;
import stanl_2.weshareyou.domain.member.aggregate.history.LoginHistory;
import stanl_2.weshareyou.domain.member.aggregate.vo.response.findlikeboard.BoardLikesResponseVO;
import stanl_2.weshareyou.domain.member.aggregate.vo.response.findlikeboard.LikeNoResponseVO;
import stanl_2.weshareyou.domain.member.aggregate.vo.response.findmyboard.MyBoardResponseVO;
import stanl_2.weshareyou.domain.member.aggregate.vo.response.findmycomment.MyCommentResponseVO;
import stanl_2.weshareyou.domain.member.repository.HistoryRepository;
import stanl_2.weshareyou.domain.member.repository.MemberRepository;
import stanl_2.weshareyou.domain.s3.S3uploader;
import stanl_2.weshareyou.global.common.exception.CommonException;
import stanl_2.weshareyou.global.common.exception.ErrorCode;
import stanl_2.weshareyou.global.security.constants.ApplicationConstants;
import stanl_2.weshareyou.global.security.service.userdetail.MemberDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service("MemberServiceImpl")
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final HistoryRepository historyRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationConstants applicationConstants;
    private final S3uploader s3uploader;
    private static final long JWT_EXPIRATION_TIME = 30000000L;

    private Timestamp getCurrentTimestamp() {
        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return Timestamp.from(nowKst.toInstant());
    }

    @Override
    @Transactional
    public MemberDTO registMember(MemberDTO memberRequestDTO) {
        Timestamp currentTimestamp = getCurrentTimestamp();

        // 비밀번호 해싱
        String hashPwd = passwordEncoder.encode(memberRequestDTO.getPassword());
        memberRequestDTO.setPassword(hashPwd);

        Member registMember = modelMapper.map(memberRequestDTO, Member.class);
        registMember.setUpdatedAt(currentTimestamp);
        registMember.setCreatedAt(currentTimestamp);

        registMember.setRole(Role.ROLE_MEMBER);
        registMember.setActive(true);

        Member newMember = memberRepository.save(registMember);

        return modelMapper.map(newMember, MemberDTO.class);
    }


    @Override
    @Transactional
    public Optional<MemberDTO> findMemberDetail(String username) {
        return memberRepository.findByLoginId(username);
    }


    @Override
    @Transactional
    public String loginMember(Authentication authenticationResponse) {

        String jwt = "";
        if (authenticationResponse != null && authenticationResponse.isAuthenticated()) {

            // 비밀키 생성 및 JWT 생성
            String secret = applicationConstants.getJWT_SECRET_DEFAULT_VALUE();
            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            MemberDetails memberDetails = (MemberDetails) authenticationResponse.getPrincipal();
            Member member = memberDetails.getMember();  // MemberDetails에서 Member를 얻어옴

            jwt = Jwts.builder()
                    .setIssuer("STANL2")
                    .setSubject("JWT Token")
                    .claim("id", member.getId())
                    .claim("loginId", authenticationResponse.getName())
                    .claim("nationality", member.getNationality())
                    .claim("sex", member.getSex())
                    .claim("point", member.getPoint())
                    .claim("nickname", member.getNickname())
                    .claim("profile", member.getProfileUrl())
                    .claim("introduction", member.getIntroduction())
                    .claim("language", member.getLanguage())
                    .claim("authorities", authenticationResponse.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                    .setIssuedAt(new java.util.Date())
                    .setExpiration(new java.util.Date((new java.util.Date()).getTime() + JWT_EXPIRATION_TIME)) // 만료시간 8시간
                    .signWith(secretKey)
                    .compact(); // Digital Signature 생성

        }
        return jwt;
    }


    @Override
    @Transactional
    public void deleteMember(Long id) {
        Timestamp currentTimestamp = getCurrentTimestamp();

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND)
                );

        member.setUpdatedAt(currentTimestamp);
        member.setActive(false);

        memberRepository.save(member);
    }


    @Override
    @Transactional
    public void updatePwd(MemberDTO memberRequestDTO) {
        Timestamp currentTimestamp = getCurrentTimestamp();

        // 찾기 수정해야함!
        Optional<Member> member2 = memberRepository.findByloginId(memberRequestDTO.getLoginId());
        Member member = member2.orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        String hashPwd = passwordEncoder.encode(memberRequestDTO.getPassword());

        member.setPassword(hashPwd);
        member.setUpdatedAt(currentTimestamp);

        memberRepository.save(member);
    }


    @Override
    @Transactional
    public MemberDTO updateProfile(MemberDTO requestMemberDTO, MultipartFile file) {
        Timestamp currentTimestamp = getCurrentTimestamp();

        Member member = memberRepository.findById(requestMemberDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        member.setNickname(requestMemberDTO.getNickname());
        member.setIntroduction(requestMemberDTO.getIntroduction());
        member.setLanguage(requestMemberDTO.getLanguage());
        member.setUpdatedAt(currentTimestamp);


        // 프로필 이미지을 변경하지 않는 경우
        if(file == null){
            memberRepository.save(member);
            MemberDTO responseMemberDTO = modelMapper.map(member, MemberDTO.class);

            // 보안상 null
            responseMemberDTO.setId(null);
            responseMemberDTO.setPassword(null);
            responseMemberDTO.setActive(null);

            return responseMemberDTO;
        }

        // s3 이미지 저장
        if(member.getProfileUrl() == null || member.getProfileUrl().isEmpty()){
            // 기존 프로필 이미지 없는 경우
            String url = s3uploader.uploadOneImage(file);
            member.setProfileUrl(url);
        }else{
            // 기존 프로필 이미지 있는 경우
            String key = member.getProfileUrl();
            if(key == null){
                throw new CommonException(ErrorCode.BAD_REQUEST_IMAGE);
            }
            s3uploader.deleteImg(key);
            String url = s3uploader.uploadOneImage(file);
            member.setProfileUrl(url);
        }

        memberRepository.save(member);

        MemberDTO responseMemberDTO = modelMapper.map(member, MemberDTO.class);

        // 보안상 null
        responseMemberDTO.setId(null);
        responseMemberDTO.setPassword(null);
        responseMemberDTO.setActive(null);

        return responseMemberDTO;
    }


    @Override
    @Transactional
    public MemberDTO updateMypage(MemberDTO requestMemberDTO) {
        Timestamp currentTimestamp = getCurrentTimestamp();

        Member member = memberRepository.findById(requestMemberDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        member.setPhone(requestMemberDTO.getPhone());
        member.setUpdatedAt(currentTimestamp);

        Member updataMember = memberRepository.save(member);

        MemberDTO responseMemberDTO = modelMapper.map(updataMember, MemberDTO.class);

        // 보안상 null
        responseMemberDTO.setId(null);
        responseMemberDTO.setPassword(null);
        responseMemberDTO.setActive(null);

        return responseMemberDTO;
    }


    @Override
    @Transactional
    public MemberDTO earnPoint(MemberDTO requestMemberDTO) {
        Timestamp currentTimestamp = getCurrentTimestamp();

        Member member = memberRepository.findById(requestMemberDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        member.setPoint(member.getPoint() + requestMemberDTO.getPoint());
        member.setUpdatedAt(currentTimestamp);

        memberRepository.save(member);

        MemberDTO responseMemberDTO = modelMapper.map(member, MemberDTO.class);

        // 보안상 null
        responseMemberDTO.setId(null);
        responseMemberDTO.setPassword(null);
        responseMemberDTO.setActive(null);

        return responseMemberDTO;
    }


    @Override
    @Transactional
    public MemberDTO findId(MemberDTO requestMemberDTO) {
        MemberDTO responseMemberDTO = getMemberDTO(requestMemberDTO);

        return responseMemberDTO;
    }


    @Override
    @Transactional
    public MemberDTO findProfile(MemberDTO requestMemberDTO) {
        MemberDTO responseMemberDTO = getMemberDTO(requestMemberDTO);

        return responseMemberDTO;
    }

    @Override
    @Transactional
    public void saveLoginHistory(HistoryInput historyInput) {
        LoginHistory loginHistory = LoginHistory.builder()
                .loginId(historyInput.getUserId())
                .loginDate(LocalDateTime.now())
                .clientIp(historyInput.getClientIp())
                .userAgent(historyInput.getUserAgent())
                .build();

        historyRepository.save(loginHistory);
    }



    @Override
    @Transactional
    public MemberDTO findMypage(MemberDTO requestMemberDTO) {
        MemberDTO responseMemberDTO = getMemberDTO(requestMemberDTO);

        return responseMemberDTO;
    }


    @Override
    @Transactional
    public MemberDTO findPoint(MemberDTO requestMemberDTO) {
        MemberDTO responseMemberDTO = getMemberDTO(requestMemberDTO);

        return responseMemberDTO;
    }


    @Override
    @Transactional
    public MemberDTO findMyBoard(MemberDTO requestMemberDTO) {

        Member member = memberRepository.findById(requestMemberDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        MemberDTO responseMemberDTO = modelMapper.map(member, MemberDTO.class);

        List<MyBoardResponseVO> boardResponseList = new ArrayList<>();
        for (Board board : member.getBoard()) {
            MyBoardResponseVO boardResponse = modelMapper.map(board, MyBoardResponseVO.class);
            boardResponseList.add(boardResponse);
        }

        responseMemberDTO.setBoard(boardResponseList);

        // 보안상 null
        responseMemberDTO.setId(null);
        responseMemberDTO.setPassword(null);
        responseMemberDTO.setActive(null);

        return responseMemberDTO;
    }


    @Override
    @Transactional
    public MemberDTO findLikeBoard(MemberDTO requestMemberDTO) {
        Member member = memberRepository.findById(requestMemberDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        // 게시판 목록
        List<Board> likedBoards = new ArrayList<>();
        for (BoardLike boardLike : member.getBoardlike()) {
            likedBoards.add(boardLike.getBoard());
        }

        // BoardLikesResponseVO 생성
        List<BoardLikesResponseVO> boardLikesResponseVOs = new ArrayList<>();
        for (Board board : likedBoards) {
            BoardLikesResponseVO boardLikesResponseVO = new BoardLikesResponseVO(
                    board.getTitle(),
                    board.getContent(),
                    board.getCommentCount(),
                    board.getLikesCount()
            );
            boardLikesResponseVOs.add(boardLikesResponseVO);
        }

        // LikeNoResponseVO 생성
        List<LikeNoResponseVO> likeNoResponseList = new ArrayList<>();
        for (BoardLikesResponseVO boardLikesResponseVO : boardLikesResponseVOs) {
            LikeNoResponseVO likeNoResponseVO = new LikeNoResponseVO();
            likeNoResponseVO.setBoardLikes(Collections.singletonList(boardLikesResponseVO));  // BoardLikesResponseVO를 리스트로 설정
            likeNoResponseList.add(likeNoResponseVO);
        }

        MemberDTO responseMemberDTO = new MemberDTO();
        responseMemberDTO.setNickname(member.getNickname());
        responseMemberDTO.setBoardLike(likeNoResponseList);

        return responseMemberDTO;
    }


    @Override
    @Transactional
    public MemberDTO findMyComment(MemberDTO requestMemberDTO) {
        Member member = memberRepository.findById(requestMemberDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        MemberDTO responseMemberDTO = modelMapper.map(member, MemberDTO.class);

        List<MyCommentResponseVO> commentResponseList = new ArrayList<>();
        for (BoardComment boardComment : member.getBoardComment()) {
            MyCommentResponseVO boardCommentResponse = modelMapper.map(boardComment, MyCommentResponseVO.class);
            commentResponseList.add(boardCommentResponse);
        }

        responseMemberDTO.setBoardComment(commentResponseList);

        // 보안상 null
        responseMemberDTO.setId(null);
        responseMemberDTO.setPassword(null);
        responseMemberDTO.setActive(null);

        return responseMemberDTO;
    }


    @Override
    @Transactional
    public MemberDTO checkMember(MemberDTO requestMemberDTO) {

        Member member = memberRepository.findByPhone(requestMemberDTO.getPhone());

        if(member == null || !requestMemberDTO.getName().equals(member.getName())) {
            throw new CommonException(ErrorCode.MEMBER_NOT_FOUND);
        }

        MemberDTO responseMemberDTO = modelMapper.map(member, MemberDTO.class);

        // 보안상 null
        responseMemberDTO.setId(null);
        responseMemberDTO.setPassword(null);
        responseMemberDTO.setActive(null);

        return responseMemberDTO;
    }


    private MemberDTO getMemberDTO(MemberDTO requestMemberDTO) {
        Member member = memberRepository.findById(requestMemberDTO.getId())
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));

        MemberDTO responseMemberDTO = modelMapper.map(member, MemberDTO.class);

        // 보안상 null
        responseMemberDTO.setId(null);
        responseMemberDTO.setPassword(null);
        responseMemberDTO.setActive(null);
        return responseMemberDTO;
    }

    private String getKey(String url){
        for(int i=0;i<url.length()-15;i++){
            if(url.substring(i, i+15).equals(".amazonaws.com/")){
                return url.substring(i+15, url.length());
            }
        }
        return null;
    }
    @Override
    @Transactional

    public MemberDTO findOtherProfile(String nickname) {
        log.info(nickname);
        Member otherMember = memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new CommonException(ErrorCode.MEMBER_NOT_FOUND));
        MemberDTO responseMemberDTO = modelMapper.map(otherMember, MemberDTO.class);

        // 보안상 null
        responseMemberDTO.setId(null);
        responseMemberDTO.setPassword(null);
        responseMemberDTO.setActive(null);
        return responseMemberDTO;

    }

}
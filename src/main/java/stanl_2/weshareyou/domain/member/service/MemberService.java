package stanl_2.weshareyou.domain.member.service;

import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import stanl_2.weshareyou.domain.member.aggregate.dto.MemberDTO;
import stanl_2.weshareyou.domain.member.aggregate.history.HistoryInput;

import java.util.Optional;

public interface MemberService {
    MemberDTO registMember(MemberDTO memberRequestDTO);

    Optional<MemberDTO> findMemberDetail(String username);

    String loginMember(Authentication authenticationResponse);

    void deleteMember(Long id);

    void updatePwd(MemberDTO memberRequestDTO);

    MemberDTO updateProfile(MemberDTO requestMemberDTO, MultipartFile image);

    MemberDTO updateMypage(MemberDTO requestMemberDTO);

    MemberDTO earnPoint(MemberDTO requestMemberDTO);

    MemberDTO findId(MemberDTO requestMemberDTO);

    MemberDTO findMypage(MemberDTO requestMemberDTO);

    MemberDTO findPoint(MemberDTO requestMemberDTO);

    MemberDTO findMyBoard(MemberDTO requestMemberDTO);

    MemberDTO findLikeBoard(MemberDTO requestMemberDTO);

    MemberDTO findMyComment(MemberDTO requestMemberDTO);

    MemberDTO checkMember(MemberDTO requestMemberDTO);

    MemberDTO findProfile(MemberDTO requestMemberDTO);
  
    void saveLoginHistory(HistoryInput historyInput);

    MemberDTO findOtherProfile(String nickname);
}
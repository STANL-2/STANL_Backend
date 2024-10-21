package stanl_2.weshareyou.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stanl_2.weshareyou.domain.member.aggregate.dto.MemberDTO;
import stanl_2.weshareyou.domain.member.aggregate.entity.Member;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByloginId(String loginId);

    Optional<MemberDTO> findByLoginId(String loginId);

    Member findByPhone(String phone);

    Optional<Member> findByNickname(String nickname);

    String findProfileUrlByNickname(String sender);
}

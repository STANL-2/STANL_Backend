package stanl_2.weshareyou.domain.member.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import stanl_2.weshareyou.domain.member.aggregate.Role;
import stanl_2.weshareyou.domain.member.aggregate.Sex;
import stanl_2.weshareyou.domain.member.aggregate.dto.MemberDTO;
import stanl_2.weshareyou.domain.member.aggregate.entity.Member;
import stanl_2.weshareyou.domain.member.repository.MemberRepository;
import stanl_2.weshareyou.global.security.constants.ApplicationConstants;
import stanl_2.weshareyou.global.security.service.userdetail.MemberDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@SpringBootTest
class MemberServiceImplTests {

    @Mock
    private ApplicationConstants applicationConstants;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    @DisplayName("회원가입 테스트")
    @Test
    void registMember() {

        // given: 회원 정보 설정
        MemberDTO given = new MemberDTO();
        given.setLoginId("testUser");
        given.setPassword("password123");
        given.setName("John Doe");
        given.setAge(21);
        given.setNationality("KOREA");
        given.setSex(Sex.FEMALE);
        given.setPhone("01012345678");
        given.setPoint(100);
        given.setRole(Role.ROLE_MEMBER);
        given.setNickname("johnny");
        given.setLanguage("English");

        Member mappedMember = new Member();
        mappedMember.setLoginId("testUser");
        mappedMember.setPassword("hashedPassword");

        Member savedMember = new Member();
        savedMember.setId(1L);
        savedMember.setLoginId("testUser");
        savedMember.setPassword("hashedPassword");
        savedMember.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        savedMember.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        savedMember.setRole(Role.ROLE_MEMBER);
        savedMember.setActive(true);

        MemberDTO expectedDTO = new MemberDTO();
        expectedDTO.setId(1L);
        expectedDTO.setLoginId("testUser");
        expectedDTO.setNickname("johnny");
        expectedDTO.setRole(Role.ROLE_MEMBER);

        // Mock 동작
        when(passwordEncoder.encode(given.getPassword())).thenReturn("해싱된 패스워드");
        when(modelMapper.map(given, Member.class)).thenReturn(mappedMember);
        when(memberRepository.save(mappedMember)).thenReturn(savedMember);
        when(modelMapper.map(savedMember, MemberDTO.class)).thenReturn(expectedDTO);

        // When
        MemberDTO result = memberService.registMember(given);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testUser", result.getLoginId());
        assertEquals("johnny", result.getNickname());
        assertEquals(Role.ROLE_MEMBER, result.getRole());

        // Mock 호출
        verify(passwordEncoder, times(1)).encode("password123");
        verify(memberRepository, times(1)).save(mappedMember);
        verify(modelMapper, times(2)).map(any(), any());
    }

    @DisplayName("로그인 후 JWT 토큰 테스트")
    @Test
    void loginMember() {
        // Given: Authentication 객체, 사용자 정보
        Member mockMember = new Member();
        mockMember.setId(1L);
        mockMember.setLoginId("testUser");
        mockMember.setNationality("Korea");
        mockMember.setSex(Sex.FEMALE);
        mockMember.setPoint(100);
        mockMember.setNickname("johnny");
        mockMember.setProfileUrl("https://profile.com/johnny.jpg");
        mockMember.setIntroduction("Hello!");
        mockMember.setLanguage("English");

        MemberDetails memberDetails = new MemberDetails(mockMember);
        Authentication authentication = mock(Authentication.class);

        // Mock 동작
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(memberDetails);
        when(authentication.getName()).thenReturn("testUser");
        when(authentication.getAuthorities())
                .thenReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_MEMBER")));

        // 비밀키 모킹
        String secretKey = "mySuperSecretKeyForJWTTokenThatIsLongEnough";
        when(applicationConstants.getJWT_SECRET_DEFAULT_VALUE()).thenReturn(secretKey);

        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // When: loginMember 호출
        String jwt = memberService.loginMember(authentication);

        // Then
        assertNotNull(jwt);
        assertFalse(jwt.isEmpty());

        // JWT 토큰의 내용 검증
        var claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody();

        assertEquals("testUser", claims.get("loginId"));
        assertEquals("Korea", claims.get("nationality"));
        assertEquals("johnny", claims.get("nickname"));
        assertEquals("Hello!", claims.get("introduction"));
        assertEquals("English", claims.get("language"));
        assertEquals("ROLE_MEMBER", claims.get("authorities"));
    }

    @DisplayName("회원 비활성화 테스트")
    @Test
    void deleteMember() {
        // Given: Member 객체
        Member member = new Member();
        member.setId(1L);
        member.setActive(true);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // When: 회원 삭제 메서드 호출
        memberService.deleteMember(1L);

        // Then
        assertFalse(member.getActive());
        verify(memberRepository, times(1)).save(member);
    }
}
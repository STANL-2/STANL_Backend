//package stanl_2.weshareyou.domain.member.service;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.modelmapper.ModelMapper;
//import org.springframework.boot.test.context.SpringBootTest;
//import stanl_2.weshareyou.domain.member.aggregate.dto.HistoryDTO;
//import stanl_2.weshareyou.domain.member.aggregate.history.LoginHistory;
//import stanl_2.weshareyou.domain.member.repository.HistoryRepository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//class HistoryServiceImplTests {
//
//
//    @Mock
//    private ModelMapper modelMapper;
//
//    @InjectMocks
//    private HistoryServiceImpl historyService;
//
//    @Mock
//    private HistoryRepository historyRepository;
//
//    @DisplayName("사용자 로그인 IP 로그 테스트")
//    @Test
//    void findIpSuccess() {
//
//        // Given
//        LoginHistory history1 = new LoginHistory();
//        history1.setId(1L);
//        history1.setLoginId("user1");
//        history1.setLoginDate(LocalDateTime.now());
//        history1.setClientIp("192.168.0.1");
//        history1.setUserAgent("Mozilla/5.0");
//
//        LoginHistory history2 = new LoginHistory();
//        history2.setId(2L);
//        history2.setLoginId("user2");
//        history2.setLoginDate(LocalDateTime.now());
//        history2.setClientIp("192.168.0.2");
//        history2.setUserAgent("Chrome/90.0");
//
//        List<LoginHistory> loginHistories = List.of(history1, history2);
//
//        HistoryDTO dto1 = new HistoryDTO(1L, "user1", history1.getLoginDate(), "192.168.0.1", "Mozilla/5.0");
//        HistoryDTO dto2 = new HistoryDTO(2L, "user2", history2.getLoginDate(), "192.168.0.2", "Chrome/90.0");
//
//        // When: Mock 동작
//        when(historyRepository.findAll()).thenReturn(loginHistories);
//        when(modelMapper.map(history1, HistoryDTO.class)).thenReturn(dto1);
//        when(modelMapper.map(history2, HistoryDTO.class)).thenReturn(dto2);
//
//        // When: 메서드 호출
//        List<HistoryDTO> result = historyService.findIp();
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals("192.168.0.1", result.get(0).getClientIp());
//        assertEquals("192.168.0.2", result.get(1).getClientIp());
//
//        // Verify: 호출 횟수 검증
//        verify(historyRepository, times(1)).findAll();
//        verify(modelMapper, times(2)).map(any(LoginHistory.class), eq(HistoryDTO.class));
//    }
//}
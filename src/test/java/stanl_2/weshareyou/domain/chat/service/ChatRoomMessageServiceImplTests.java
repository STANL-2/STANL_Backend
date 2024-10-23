//package stanl_2.weshareyou.domain.chat.service;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import stanl_2.weshareyou.domain.chat.entity.ChatRoomMessage;
//import stanl_2.weshareyou.domain.chat.repository.ChatRoomMessageRepository;
//
//import java.sql.Timestamp;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//class ChatRoomMessageServiceImplTests {
//
//    @MockBean
//    private ChatRoomMessageRepository chatRoomMessageRepository;
//
//    @Autowired
//    private ChatRoomMessageServiceImpl chatRoomMessageService;
//
//    @Test
//    @DisplayName("채팅방 메시지 로딩 성공 테스트")
//    void testGetMessagesByRoomId() {
//        // given
//        String roomId = "roomId123";
//        ChatRoomMessage chatRoomMessage = new ChatRoomMessage();
//        chatRoomMessage.setRoomId(roomId);
//
//        ChatRoomMessage.Message message = new ChatRoomMessage.Message();
//        message.setSender("sender1");
//        message.setMessage("Hello!");
//        message.setReadYn(false);
//        message.setCreatedAt(new Timestamp(System.currentTimeMillis()));
//
//        chatRoomMessage.setMessages(new ArrayList<>(List.of(message)));
//
//        // when
//        when(chatRoomMessageRepository.findByRoomId(roomId)).thenReturn(chatRoomMessage);
//
//        // execute
//        ChatRoomMessage result = chatRoomMessageService.getMessagesByRoomId(roomId);
//
//        // then
//        assertNotNull(result);
//        assertEquals(roomId, result.getRoomId());
//        assertEquals(1, result.getMessages().size());
//        assertEquals("sender1", result.getMessages().get(0).getSender());
//        assertEquals("Hello!", result.getMessages().get(0).getMessage());
//        verify(chatRoomMessageRepository, times(1)).findByRoomId(roomId);
//    }
//
//    @Test
//    @DisplayName("채팅방 메시지 로딩 실패 테스트")
//    void testGetMessagesByRoomIdWhenNoMessages() {
//        // given
//        String roomId = "roomId123";
//
//        // when
//        when(chatRoomMessageRepository.findByRoomId(roomId)).thenReturn(null);
//
//        // execute
//        ChatRoomMessage result = chatRoomMessageService.getMessagesByRoomId(roomId);
//
//        // then
//        assertNotNull(result);
//        assertEquals(roomId, result.getRoomId());
//        assertTrue(result.getMessages().isEmpty());
//        verify(chatRoomMessageRepository, times(1)).findByRoomId(roomId);
//    }
//}
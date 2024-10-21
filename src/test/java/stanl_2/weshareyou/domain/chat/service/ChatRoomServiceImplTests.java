package stanl_2.weshareyou.domain.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import stanl_2.weshareyou.domain.chat.entity.ChatRoom;
import stanl_2.weshareyou.domain.chat.repository.ChatRoomRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ChatRoomServiceImplTests {

    @MockBean
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRoomServiceImpl chatRoomService;

    @Test
    @DisplayName("채팅방 생성 테스트")
    void testCreateChatRoom() {
        // given
        String sender = "senderUser";
        String receiver = "receiverUser";
        String senderProfileUrl = "senderUrl";
        String receiverProfileUrl = "receiverUrl";
        ChatRoom chatRoom = ChatRoom.create(sender, receiver, senderProfileUrl, receiverProfileUrl);

        // when
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);
        ChatRoom result = chatRoomService.createChatRoom(sender, receiver, senderProfileUrl, receiverProfileUrl);

        // then
        assertNotNull(result);
        assertEquals(sender, result.getSender());
        assertEquals(receiver, result.getReceiver());
        assertEquals(receiver, result.getSenderProfileUrl());
        assertEquals(receiver, result.getReceiverProfileUrl());
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));

    }
}
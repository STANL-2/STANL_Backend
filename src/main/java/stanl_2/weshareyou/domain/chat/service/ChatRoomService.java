package stanl_2.weshareyou.domain.chat.service;

import org.springframework.stereotype.Service;
import stanl_2.weshareyou.domain.chat.entity.ChatRoom;

import java.util.List;

@Service
public interface ChatRoomService {
    List<ChatRoom> findRoomsByUser(String user);

    ChatRoom findRoomById(String roomId);

    ChatRoom createChatRoom(String sender, String receiver,String senderProfileUrl,String receiverProfileUrl);

    Boolean deleteChatRoom(String roomId, String nickname);
}
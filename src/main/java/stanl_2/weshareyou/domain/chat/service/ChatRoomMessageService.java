package stanl_2.weshareyou.domain.chat.service;

import org.springframework.stereotype.Service;
import stanl_2.weshareyou.domain.chat.entity.ChatRoomMessage;

@Service
public interface ChatRoomMessageService {
    ChatRoomMessage getMessagesByRoomId(String roomId);
    void addMessageToRoom(String roomId, String sender, String message);
    void markMessagesAsRead(String roomId, String nickname);
}
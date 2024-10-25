package stanl_2.weshareyou.domain.chat.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stanl_2.weshareyou.domain.chat.entity.ChatMessage;
import stanl_2.weshareyou.domain.chat.service.ChatRoomMessageService;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("")
public class ChatController {

    private final ChatRoomMessageService chatRoomMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/message/{roomId}")
    public void sendMessage(ChatMessage message, @DestinationVariable String roomId) {
        try {
            chatRoomMessageService.addMessageToRoom(message.getRoomId(), message.getSender(), message.getMessage());
            // 구독 경로를 하나 더 추가하면 알림에 뜨게 할 수 있을 듯?
            messagingTemplate.convertAndSend("/sub/" + roomId, message);

            //알림 메시지 전송
            ChatMessage notificationMessage = createNotificationMessage(message);
            messagingTemplate.convertAndSend("/sub/notification/" + roomId, notificationMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ChatMessage createNotificationMessage(ChatMessage message){
        ChatMessage notificationMessage = new ChatMessage();

        notificationMessage.setSendTime(message.getSendTime());
        notificationMessage.setSender(message.getSender());
        notificationMessage.setMessage(message.getMessage());
        return notificationMessage;
    }
}
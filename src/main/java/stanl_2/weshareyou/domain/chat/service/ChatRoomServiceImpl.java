package stanl_2.weshareyou.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stanl_2.weshareyou.domain.chat.entity.ChatRoom;
import stanl_2.weshareyou.domain.chat.repository.ChatRoomRepository;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatRoomServiceImpl implements ChatRoomService{

    private final ChatRoomRepository chatRoomRepository;

    @Override
    @Transactional
    public List<ChatRoom> findRoomsByUser(String user) {
        return chatRoomRepository.findAll().stream()
                .filter(room -> (room.getSender() != null && room.getSender().equals(user) && !room.getSenderDelete()) ||
                        (room.getReceiver() != null && room.getReceiver().equals(user) && !room.getReceiverDelete()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatRoom findRoomById(String roomId) {
        return chatRoomRepository.findByRoomId(roomId);
    }

    @Override
    @Transactional
    public ChatRoom createChatRoom(String sender, String receiver,String senderProfileUrl,String receiverProfileUrl) {
        ChatRoom chatRoom = ChatRoom.create(sender, receiver,senderProfileUrl,receiverProfileUrl);
        chatRoomRepository.save(chatRoom); // DB에 저장
        return chatRoom;
    }

    @Override
    @Transactional
    public Boolean deleteChatRoom(String roomId, String nickname) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId);

        /* 설명. sender와 nickname이 같을 때 senderDelete = false */
        if(chatRoom.getSender().equals(nickname)){
            chatRoom.setSenderDelete(true);
        }else{
            chatRoom.setReceiverDelete(true);
        }

        chatRoomRepository.save(chatRoom);

        return true;
    }
}
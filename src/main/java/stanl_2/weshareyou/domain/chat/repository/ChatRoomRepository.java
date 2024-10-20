package stanl_2.weshareyou.domain.chat.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import stanl_2.weshareyou.domain.chat.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

@Repository("mongoChatRoomRepository")
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    ChatRoom findByRoomId(String roomId);
}

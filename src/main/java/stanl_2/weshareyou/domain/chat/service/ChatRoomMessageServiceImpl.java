package stanl_2.weshareyou.domain.chat.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stanl_2.weshareyou.domain.chat.entity.ChatRoomMessage;
import stanl_2.weshareyou.domain.chat.repository.ChatRoomMessageRepository;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@RequiredArgsConstructor
@Service
@Slf4j
public class ChatRoomMessageServiceImpl implements ChatRoomMessageService {

    private final ChatRoomMessageRepository chatRoomMessageRepository;
    private final MongoTemplate mongoTemplate;
    private Timestamp getCurrentTimestamp() {
        ZonedDateTime nowKst = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return Timestamp.from(nowKst.toInstant());
    }

    // 특정 채팅방의 메시지 조회
    @Override
    @Transactional
    public ChatRoomMessage getMessagesByRoomId(String roomId) {

        ChatRoomMessage chatRoomMessage = chatRoomMessageRepository.findByRoomId(roomId);
        if (chatRoomMessage == null) {
            log.warn("No messages found for roomId: " + roomId);
            chatRoomMessage = new ChatRoomMessage();
            chatRoomMessage.setRoomId(roomId);
            chatRoomMessage.setMessages(new ArrayList<>()); // 빈 리스트로 초기화
        }
        return chatRoomMessage;
    }

    // 특정 채팅방에 메시지 추가 (업데이트 방식으로)
    @Override
    @Transactional
    public void addMessageToRoom(String roomId, String sender, String message) {
        ChatRoomMessage.Message newMessage = new ChatRoomMessage.Message();
        Timestamp currentTimestamp = getCurrentTimestamp();

        newMessage.setSender(sender);
        newMessage.setMessage(message);
        newMessage.setReadYn(false);
        newMessage.setCreatedAt(currentTimestamp);

        // MongoDB 쿼리로 부분 업데이트 수행
        Query query = new Query(Criteria.where("roomId").is(roomId));
        Update update = new Update().push("messages", newMessage);

        // document가 있으면 업데이트, 없으면 생성
        mongoTemplate.upsert(query, update, ChatRoomMessage.class);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String roomId, String nickname) {

        MongoCollection<Document> collection = mongoTemplate.getCollection("roomMessages");
        // 쿼리: roomId에 해당하는 문서 찾기
        Document query = new Document("roomId", roomId);
        // 업데이트: sender가 지정된 사용자가 아니고 readYn이 false인 메시지의 readYn을 true로 설정
        Document update = new Document("$set", new Document("messages.$[elem].readYn", true));
        // 배열 필터 정의
        List<Document> arrayFilters = Collections.singletonList(
                new Document("elem.sender", new Document("$ne", nickname))
                        .append("elem.readYn", false)
        );
        // UpdateOptions 설정
        UpdateOptions options = new UpdateOptions().arrayFilters(arrayFilters);
        // MongoDB에서 배열 필드를 업데이트
        UpdateResult result = collection.updateMany(query, update, options);

    }

}
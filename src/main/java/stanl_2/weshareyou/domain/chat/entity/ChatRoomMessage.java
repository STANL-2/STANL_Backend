package stanl_2.weshareyou.domain.chat.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "roomMessages") // 하나의 document에 여러 메시지를 저장하는 구조
public class ChatRoomMessage {

    @Id
    private String roomId;

    // 메시지와 sender 정보를 리스트로 저장
    private List<Message> messages = new ArrayList<>();

    @Getter
    @Setter
    public static class Message {
        private String sender;
        private String message;
        /* 설명. 읽음여부 */
        private Boolean readYn;
        /* 설명. 보낸시각 */
        private Timestamp createdAt;
    }
}
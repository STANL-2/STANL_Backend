package stanl_2.weshareyou.domain.chat.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.UUID;


@Getter
@Setter
@Document(collection = "room")
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    @Id
    private String id;
    private String roomId;
    private String senderProfileUrl;
    private String sender;
    private String receiverProfileUrl;
    private String receiver;
    /* 설명. 참여자가 들어와있는지 여부 컬럼 */
    private Boolean senderActive = false;
    private Boolean receiverActive = false;
    /* 설명. 삭제여부 */
    private Boolean senderDelete = false;
    private Boolean receiverDelete = false;

    public static ChatRoom create(String sender, String receiver,String senderProfileUrl,String receiverProfileUrl) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomId = UUID.randomUUID().toString();
        /* 설명. 1대1 채팅방 생성*/
        chatRoom.sender = sender;
        chatRoom.receiver = receiver;
        chatRoom.senderProfileUrl=senderProfileUrl;
        chatRoom.receiverProfileUrl=receiverProfileUrl;
        return chatRoom;
    }

}

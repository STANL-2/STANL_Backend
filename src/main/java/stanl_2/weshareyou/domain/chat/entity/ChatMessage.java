package stanl_2.weshareyou.domain.chat.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Getter
@Setter
@Document(collection  = "message")
public class ChatMessage {

    private String roomId;
    private String sender;
    private String message;
    private Date sendTime;
}

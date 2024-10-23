package stanl_2.weshareyou.domain.board_comment.aggregate.dto;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardCommentDto {
        private Long boardCommentId;
        private Long boardId;
        private Long memberId;
        private String nickname;
        private String content;
        private Timestamp createdAt;
        private Timestamp updatedAt;

        private String memberProfileUrl;
}

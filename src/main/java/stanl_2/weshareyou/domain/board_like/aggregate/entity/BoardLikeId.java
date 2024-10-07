package stanl_2.weshareyou.domain.board_like.aggregate.entity;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
public class BoardLikeId implements Serializable {

    private Long memberId;

    private Long boardId;  // Long 타입의 boardId 그대로 유지
}

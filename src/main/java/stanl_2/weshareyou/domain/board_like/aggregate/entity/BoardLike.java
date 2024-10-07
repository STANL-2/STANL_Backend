package stanl_2.weshareyou.domain.board_like.aggregate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name="BoardLike")
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BoardLikeId.class)
public class BoardLike {
    @Id
    @Column(name ="member_id")
    private Long memberId;

    @Id
    @Column(name ="board_id")
    private Long boardId;
}

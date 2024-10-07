package stanl_2.weshareyou.domain.board_like.aggregate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import stanl_2.weshareyou.domain.board.aggregate.entity.Board;

@Entity
@Table(name="boardLike")
@NoArgsConstructor
@AllArgsConstructor
@IdClass(BoardLikeId.class)
public class BoardLike {

    @Id
    @Column(name ="MEMBER_ID")
    private Long memberId;

    @Id
    @Column(name ="BOARD_ID")
    private Long boardId;  // Board의 ID 필드만 저장

    @ManyToOne
    @JoinColumn(name = "BOARD_ID", referencedColumnName = "BOARD_ID") // 수정
    private Board board;
}

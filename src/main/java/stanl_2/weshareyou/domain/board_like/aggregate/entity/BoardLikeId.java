package stanl_2.weshareyou.domain.board_like.aggregate.entity;

import jakarta.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
public class BoardLikeId implements Serializable {

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long boarId;
}

package stanl_2.weshareyou.domain.board_image.aggregate.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import stanl_2.weshareyou.domain.board.aggregate.entity.Board;

import java.util.ArrayList;

/*
@NoArgsConstructor(force = true)
: PostMapper를 통해 Post 객체를 가져올 때는 PostMapper에 정의한 resultMap 형태로
가져오게 된다. 이 resultMap은 Image 객체를 포함하고 있는데 Post 중에는 Image가
없는 것도 있기 때문에 이 경우 기본 생성자로 Image를 생성해야 한다. 그러나 기본 생성자는
아무 파라미터도 없기 때문에 필드가 final로 정의되어 있는 경우 필드를 초기화 할 수 없어
기본 생성자를 만들 수 없고 에러가 발생한다. 이 때 force 옵션을 true로 설정하면 final
필드를 0, false, null 등으로 초기화를 강제로 시켜 생성자를 만들 수 있다.
 */

@Entity
@Table(name="BOARD_IMAGE")
@NoArgsConstructor
//@NoArgsConstructor(force = true)
@AllArgsConstructor
@ToString
@Getter
@Setter
public class BoardImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="BOARD_IMAGE_ID")
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @Column(name="BOARD_IMAGE_NAME")
    private String name;

    @ManyToOne
    @JoinColumn(name = "BOARD_ID")
    @NotNull
    private Board board;
}

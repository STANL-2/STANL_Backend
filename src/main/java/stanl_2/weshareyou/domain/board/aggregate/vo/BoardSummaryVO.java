package stanl_2.weshareyou.domain.board.aggregate.vo;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import stanl_2.weshareyou.domain.board_image.aggregate.dto.BoardImageDTO;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BoardSummaryVO {

    @NotNull
    private Long id;

    @NotNull
    private String content;

    @NotNull
    private String memberProfileUrl;

    @NotNull
    private String memberNickname;

    @NotNull
    private String title;

    @NotNull
    private List<BoardImageDTO> imageObj;

    @NotNull
    private Integer commentCount;

    @NotNull
    private Integer likesCount;

    @NotNull
    private Integer memberId;

    @NotNull
    private Boolean active;
}

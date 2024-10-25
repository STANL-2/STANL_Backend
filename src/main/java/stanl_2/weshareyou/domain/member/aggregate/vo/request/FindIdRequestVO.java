package stanl_2.weshareyou.domain.member.aggregate.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class FindIdRequestVO {
    @NotNull(message = "찾으실 아이디를 작성해주세요.")
    private String name;
}

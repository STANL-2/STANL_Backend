package stanl_2.weshareyou.domain.product.aggregate.vo.response;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.sql.Timestamp;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ProductRentalResponseVO {

    @NotNull
    private Long id;

    @NotNull
    private Long memberId;

    @NotNull
    private Boolean rental;

    @NotNull
    Timestamp startAt;

    @NotNull
    Timestamp endAt;
}

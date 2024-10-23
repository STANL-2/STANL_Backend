package stanl_2.weshareyou.domain.product.aggregate.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProductRentalRequestVO {

    @NotNull(message = "대여 시작날짜를 입력해 주세요.")
    Timestamp startAt;

    @NotNull(message = "대여 종료날짜를 입력해 주세요.")
    Timestamp endAt;
}

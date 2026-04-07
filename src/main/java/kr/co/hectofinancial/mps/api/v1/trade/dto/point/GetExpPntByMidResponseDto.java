package kr.co.hectofinancial.mps.api.v1.trade.dto.point;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC, toBuilder = true)
public class GetExpPntByMidResponseDto {

    private int totCnt;
    private List<PntExprList> data = new ArrayList<>();

    @Data @Builder
    public static class PntExprList{
        private String custNo;
        private String custId;
        private String expPntAmt;
        private String vldPd;
    }
}

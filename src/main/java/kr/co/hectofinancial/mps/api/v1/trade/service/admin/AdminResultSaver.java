package kr.co.hectofinancial.mps.api.v1.trade.service.admin;

import kr.co.hectofinancial.mps.api.v1.trade.domain.AdminTrdDtl;
import kr.co.hectofinancial.mps.api.v1.trade.repository.AdminTrdDtlRepository;
import kr.co.hectofinancial.mps.global.constant.AdminRsltStatCd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminResultSaver {

    private final AdminTrdDtlRepository adminTrdDtlRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailureOrSuccess(String failType, AdminTrdDtl adminTrdDtl, String rsltCd, String rsltMsg) {
        switch (failType) {
            case "FAIL":
                adminTrdDtl.saveResultCode(AdminRsltStatCd.FAIL.getRsltStatCd(), rsltCd, rsltMsg);
                break;
            case "SUCCESS":
                adminTrdDtl.saveResultCode(AdminRsltStatCd.SUCCESS.getRsltStatCd(), rsltCd, null);
                break;
        }
        adminTrdDtlRepository.save(adminTrdDtl);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveValue(String failType, AdminTrdDtl adminTrdDtl, String custNo, long blc){
        switch (failType) {
            case "HAS_CUST":
                adminTrdDtl.hasCustNo(custNo);
                break;
            case "HAS_NO_CUST":
                adminTrdDtl.hasNoCustNo();
                break;
            case "BF_BLC":
                adminTrdDtl.saveBfBlc(blc);
                break;
            case "AF_BLC":
                adminTrdDtl.saveAfBlc(blc);
                break;
        }
        adminTrdDtlRepository.save(adminTrdDtl);
    }
}

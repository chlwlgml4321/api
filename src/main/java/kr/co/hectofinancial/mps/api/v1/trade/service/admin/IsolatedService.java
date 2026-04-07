package kr.co.hectofinancial.mps.api.v1.trade.service.admin;

import kr.co.hectofinancial.mps.api.v1.customer.dto.CustomerDto;
import kr.co.hectofinancial.mps.api.v1.trade.domain.AdminTrdDtl;
import kr.co.hectofinancial.mps.api.v1.trade.domain.AdminTrdReq;
import kr.co.hectofinancial.mps.api.v1.trade.dto.admin.AdminWithdrawApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.admin.AdminWithdrawApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.charge.ChargeApprovalResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.point.PointRevokeRequestDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.point.PointRevokeResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.dto.wallet.CustomerWalletResponseDto;
import kr.co.hectofinancial.mps.api.v1.trade.service.charge.ApprovalService;
import kr.co.hectofinancial.mps.api.v1.trade.service.PointService;
import kr.co.hectofinancial.mps.api.v1.trade.service.money.MoneyService;
import kr.co.hectofinancial.mps.api.v1.trade.service.wallet.WalletService;
import kr.co.hectofinancial.mps.global.constant.TrdDivCd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IsolatedService {

    private final ApprovalService approvalService;
    private final WalletService walletService;
    private final MoneyService moneyService;
    private final PointService pointService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object adminTradeRequest(AdminTrdReq adminTrdReq, AdminTrdDtl adminTrdDtl, CustomerDto customerDto, String pktHash){
        Object result = null;
        try{
            switch (adminTrdReq.getTrdDivCd()){
                case "MP":
                case "PP":
                    result = chargApproval(adminTrdReq, adminTrdDtl, customerDto, pktHash);
                    break;
                case "MW":
                case "WW":
                    result = adminWithdraw(adminTrdReq, adminTrdDtl, customerDto, pktHash);
                    break;
                case "PR":
                    result = pointRevoke(adminTrdReq, adminTrdDtl, customerDto, pktHash);
                    break;
            }
            return result;
        }catch (Exception e){
//            e.printStackTrace();
            throw  e;
        }
    }

    public ChargeApprovalResponseDto chargApproval(AdminTrdReq adminTrdReq, AdminTrdDtl adminTrdDtl, CustomerDto customerDto, String pktHash){
        //충전 API 호출
        ChargeApprovalRequestDto chargeApprovalRequestDto = ChargeApprovalRequestDto.builder()
                .custNo(adminTrdDtl.getRmk())
                .customerDto(customerDto)
                .mTrdNo(adminTrdDtl.getSeqNo())
                .chrgMeanCd(adminTrdReq.getChrgMeanCd())
                .trdAmt(String.valueOf(adminTrdDtl.getTrdAmt()))
                .divCd(adminTrdReq.getTrdDivCd())
                .blcAmt(String.valueOf(adminTrdDtl.getProcBfBlc()))
                .pntVldPd(adminTrdReq.getVldPd())
                .trdSumry(adminTrdReq.getTrdReqNm())
                .chrgTrdNo(adminTrdDtl.getSeqNo())
                .trdDivDtlCd(adminTrdReq.getTrdDivDtlCd())
                .mResrvField1(adminTrdReq.getMResrvField1())
                .pktHash(pktHash)
                .build();
        return approvalService.chargeApproval(chargeApprovalRequestDto);
    }

    public AdminWithdrawApprovalResponseDto adminWithdraw(AdminTrdReq adminTrdReq, AdminTrdDtl adminTrdDtl, CustomerDto customerDto, String pktHash){
        //관리자 출금 API 호출
        AdminWithdrawApprovalRequestDto adminWithdrawApprovalRequestDto = AdminWithdrawApprovalRequestDto.builder()
                .custNo(adminTrdDtl.getRmk())
                .customerDto(customerDto)
                .mTrdNo(adminTrdDtl.getSeqNo())
                .divCd(adminTrdReq.getTrdDivCd())
                .trdAmt(String.valueOf(adminTrdDtl.getTrdAmt()))
                .blcAmt(String.valueOf(adminTrdDtl.getProcBfBlc()))
                .trdSumry(adminTrdReq.getTrdReqNm())
                .mResrvField1(adminTrdReq.getMResrvField1())
                .pktHash(pktHash)
                .build();
        return moneyService.adminWithdraw(adminWithdrawApprovalRequestDto);
    }

    public PointRevokeResponseDto pointRevoke(AdminTrdReq adminTrdReq, AdminTrdDtl adminTrdDtl, CustomerDto customerDto, String pktHash){
        //포인트 회수 API 호출
        PointRevokeRequestDto pointRevokeRequestDto = PointRevokeRequestDto.builder()
                .custNo(adminTrdDtl.getRmk())
                .customerDto(customerDto)
                .mTrdNo(adminTrdDtl.getSeqNo())
                .trdAmt(String.valueOf(adminTrdDtl.getTrdAmt()))
                .pntBlc(String.valueOf(adminTrdDtl.getProcBfBlc()))
                .trdSumry(adminTrdReq.getTrdReqNm())
                .mResrvField1(adminTrdReq.getMResrvField1())
                .pktHash(pktHash)
                .build();
        return pointService.pointRevoke(pointRevokeRequestDto);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long getCustWallet(String custNo, String divCd){

        CustomerWalletResponseDto custWallet = walletService.getCustWalletByCustNo(custNo);
        if (TrdDivCd.MONEY_PROVIDE.getTrdDivCd().equals(divCd) || TrdDivCd.MONEY_WITHDRAW.getTrdDivCd().equals(divCd) || TrdDivCd.WAITMONEY_WITHDRAW.getTrdDivCd().equals(divCd)) {
            return  custWallet.getMnyBlc();
        } else if (TrdDivCd.POINT_PROVIDE.getTrdDivCd().equals(divCd) || TrdDivCd.POINT_REVOKE.getTrdDivCd().equals(divCd)) {
            return custWallet.getPntBlc();
        } else{
            return 0;
        }
    }
}

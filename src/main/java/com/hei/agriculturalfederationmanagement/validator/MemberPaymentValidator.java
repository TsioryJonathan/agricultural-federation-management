package com.hei.agriculturalfederationmanagement.validator;

import com.hei.agriculturalfederationmanagement.entity.dto.CreateMemberPayment;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.repository.AccountRepository;
import com.hei.agriculturalfederationmanagement.repository.CotisationPlanRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MemberPaymentValidator {

    private final AccountRepository accountRepository;
    private final CotisationPlanRepository cotisationPlanRepository;


    public void validatePaymentRequest(CreateMemberPayment request, Integer collectivityId) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BadRequestException("Amount must be greater than 0");
        }

        if (request.getPaymentMode() == null) {
            throw new BadRequestException("Payment mode is required");
        }

        if (request.getAccountCreditedIdentifier() == null) {
            throw new BadRequestException("Account credited identifier is required");
        }

        Integer accountId = (request.getAccountCreditedIdentifier());
        if (!accountRepository.existsByIdAndCollectivityId(accountId, collectivityId)) {
            throw new BadRequestException("Account not found or does not belong to the member's collectivity");
        }

        if (request.getMembershipFeeIdentifier() != null) {
            Integer feeId = (request.getMembershipFeeIdentifier());
            var fee = cotisationPlanRepository.findById(feeId)
                    .orElseThrow(() -> new NotFoundException("Membership fee not found with id: " + feeId));

            if (!fee.getIdCollectivity().equals(collectivityId)) {
                throw new BadRequestException("Membership fee does not belong to the member's collectivity");
            }

            if (!fee.getIsActive()) {
                throw new BadRequestException("Membership fee is not active");
            }
        }
    }
}

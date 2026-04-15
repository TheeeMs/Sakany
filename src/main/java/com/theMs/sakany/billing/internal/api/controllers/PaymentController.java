package com.theMs.sakany.billing.internal.api.controllers;

import com.theMs.sakany.billing.internal.api.dtos.PaymentResponse;
import com.theMs.sakany.billing.internal.application.queries.ListPaymentsQuery;
import com.theMs.sakany.billing.internal.application.queries.ListPaymentsQueryHandler;
import com.theMs.sakany.billing.internal.domain.Payment;
import com.theMs.sakany.billing.internal.domain.PaymentStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {

    private final ListPaymentsQueryHandler listPaymentsQueryHandler;

    public PaymentController(ListPaymentsQueryHandler listPaymentsQueryHandler) {
        this.listPaymentsQueryHandler = listPaymentsQueryHandler;
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> listPayments(
            @RequestParam(required = false) UUID residentId,
            @RequestParam(required = false) PaymentStatus status
    ) {
        List<PaymentResponse> response = listPaymentsQueryHandler.handle(new ListPaymentsQuery(residentId, status))
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getInvoiceId(),
                payment.getResidentId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getTransactionReference(),
                payment.getStatus()
        );
    }
}

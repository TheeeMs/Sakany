package com.theMs.sakany.billing.internal.application.queries;

import com.theMs.sakany.billing.internal.domain.Payment;
import com.theMs.sakany.billing.internal.domain.PaymentRepository;
import com.theMs.sakany.shared.cqrs.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListPaymentsQueryHandler implements QueryHandler<ListPaymentsQuery, List<Payment>> {

    private final PaymentRepository paymentRepository;

    public ListPaymentsQueryHandler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public List<Payment> handle(ListPaymentsQuery query) {
        if (query.residentId() != null && query.status() != null) {
            return paymentRepository.findByResidentIdAndStatus(query.residentId(), query.status());
        }
        if (query.residentId() != null) {
            return paymentRepository.findByResidentId(query.residentId());
        }
        if (query.status() != null) {
            return paymentRepository.findByStatus(query.status());
        }
        return paymentRepository.findAll();
    }
}

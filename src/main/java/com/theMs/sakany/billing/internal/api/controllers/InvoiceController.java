package com.theMs.sakany.billing.internal.api.controllers;

import com.theMs.sakany.billing.internal.api.dtos.InvoiceResponse;
import com.theMs.sakany.billing.internal.api.dtos.IssueInvoiceRequest;
import com.theMs.sakany.billing.internal.api.dtos.PayInvoiceRequest;
import com.theMs.sakany.billing.internal.application.commands.CancelInvoiceCommand;
import com.theMs.sakany.billing.internal.application.commands.CancelInvoiceCommandHandler;
import com.theMs.sakany.billing.internal.application.commands.IssueInvoiceCommand;
import com.theMs.sakany.billing.internal.application.commands.IssueInvoiceCommandHandler;
import com.theMs.sakany.billing.internal.application.commands.PayInvoiceCommand;
import com.theMs.sakany.billing.internal.application.commands.PayInvoiceCommandHandler;
import com.theMs.sakany.billing.internal.application.queries.GetInvoiceByIdQuery;
import com.theMs.sakany.billing.internal.application.queries.GetInvoiceByIdQueryHandler;
import com.theMs.sakany.billing.internal.application.queries.ListInvoicesQuery;
import com.theMs.sakany.billing.internal.application.queries.ListInvoicesQueryHandler;
import com.theMs.sakany.billing.internal.domain.Invoice;
import com.theMs.sakany.billing.internal.domain.InvoiceStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/invoices")
public class InvoiceController {

    private final IssueInvoiceCommandHandler issueInvoiceCommandHandler;
    private final ListInvoicesQueryHandler listInvoicesQueryHandler;
    private final GetInvoiceByIdQueryHandler getInvoiceByIdQueryHandler;
    private final PayInvoiceCommandHandler payInvoiceCommandHandler;
    private final CancelInvoiceCommandHandler cancelInvoiceCommandHandler;

    public InvoiceController(
            IssueInvoiceCommandHandler issueInvoiceCommandHandler,
            ListInvoicesQueryHandler listInvoicesQueryHandler,
            GetInvoiceByIdQueryHandler getInvoiceByIdQueryHandler,
            PayInvoiceCommandHandler payInvoiceCommandHandler,
            CancelInvoiceCommandHandler cancelInvoiceCommandHandler
    ) {
        this.issueInvoiceCommandHandler = issueInvoiceCommandHandler;
        this.listInvoicesQueryHandler = listInvoicesQueryHandler;
        this.getInvoiceByIdQueryHandler = getInvoiceByIdQueryHandler;
        this.payInvoiceCommandHandler = payInvoiceCommandHandler;
        this.cancelInvoiceCommandHandler = cancelInvoiceCommandHandler;
    }

    @PostMapping
    public ResponseEntity<UUID> issueInvoice(@RequestBody IssueInvoiceRequest request) {
        IssueInvoiceCommand command = new IssueInvoiceCommand(
                request.residentId(),
                request.unitId(),
                request.type(),
                request.amount(),
                request.currency(),
                request.description(),
                request.dueDate()
        );
        UUID invoiceId = issueInvoiceCommandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceId);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> listInvoices(
            @RequestParam(required = false) UUID residentId,
            @RequestParam(required = false) InvoiceStatus status
    ) {
        List<InvoiceResponse> response = listInvoicesQueryHandler.handle(new ListInvoicesQuery(residentId, status))
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable UUID id) {
        Invoice invoice = getInvoiceByIdQueryHandler.handle(new GetInvoiceByIdQuery(id));
        return ResponseEntity.ok(toResponse(invoice));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<UUID> payInvoice(@PathVariable UUID id, @RequestBody PayInvoiceRequest request) {
        PayInvoiceCommand command = new PayInvoiceCommand(
                id,
                request.residentId(),
                request.paymentMethod(),
                request.transactionReference()
        );
        UUID paymentId = payInvoiceCommandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentId);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelInvoice(@PathVariable UUID id) {
        cancelInvoiceCommandHandler.handle(new CancelInvoiceCommand(id));
        return ResponseEntity.noContent().build();
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getResidentId(),
                invoice.getUnitId(),
                invoice.getType(),
                invoice.getAmount(),
                invoice.getCurrency(),
                invoice.getDescription(),
                invoice.getDueDate(),
                invoice.getStatus(),
                invoice.getIssuedAt(),
                invoice.getPaidAt()
        );
    }
}

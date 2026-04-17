package com.theMs.sakany.shared.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(BusinessRuleException.class)
    ProblemDetail handleBusinessRuleException(BusinessRuleException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Business Rule Violation");

        return problem;
    }

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail handleNotFoundException(NotFoundException e){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Resource Not Found");
        problem.setProperty("entity", e.getEntityName());
        problem.setProperty("id", e.getId());

        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, mapIntegrityViolationMessage(e));
        problem.setTitle("Data Integrity Violation");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleGenericException(Exception e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage() != null ? e.getMessage() : "Unknown internal error");
        problem.setTitle("Internal Server Error");
        if (e.getCause() != null) {
            problem.setProperty("cause", e.getCause().getMessage());
        }
        // Very handy for debugging 500s directly on the client side
        problem.setProperty("trace", e.getStackTrace()[0].toString());
        return problem;
    }

    private String mapIntegrityViolationMessage(DataIntegrityViolationException e) {
        String message = null;
        Throwable mostSpecific = e.getMostSpecificCause();
        if (mostSpecific != null) {
            message = mostSpecific.getMessage();
        }
        if (message == null || message.isBlank()) {
            message = e.getMessage();
        }
        if (message == null) {
            return "Request conflicts with existing data";
        }

        String normalized = message.toLowerCase(Locale.ROOT);
        if (normalized.contains("uq_resident_profiles_national_id")) {
            return "nationalId already registered";
        }
        if (normalized.contains("users_email_key") || normalized.contains("uq_users_email")) {
            return "Email already registered";
        }
        if (normalized.contains("users_phone_key") || normalized.contains("uq_users_phone")) {
            return "Phone number already registered";
        }

        return "Request conflicts with existing data";
    }
}

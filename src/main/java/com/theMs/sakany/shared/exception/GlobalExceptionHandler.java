package com.theMs.sakany.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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
}

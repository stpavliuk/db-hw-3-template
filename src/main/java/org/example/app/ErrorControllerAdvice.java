package org.example.app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.logging.Level;

@ControllerAdvice
@Log
public class ErrorControllerAdvice {

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException exception,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        log.log(Level.SEVERE, "Request failed.", exception);
        response.setStatus(exception.getStatusCode().value());
        request.setAttribute("errorMessage",
            exception.getReason() != null ? exception.getReason() : "Request could not be completed.");
        return "common/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception exception,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        log.log(Level.SEVERE, "Unexpected request failure.", exception);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        request.setAttribute("errorMessage", "An unexpected error occurred.");
        return "common/error";
    }
}

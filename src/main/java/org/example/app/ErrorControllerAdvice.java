package org.example.app;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.logging.Level;

@ControllerAdvice
@Log
public class ErrorControllerAdvice {

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException exception, HttpServletRequest request) {
        log.log(Level.SEVERE, "Request failed.", exception);
        RequestContextUtils.getOutputFlashMap(request)
            .put("errorMessage", exception.getReason() != null ? exception.getReason() : "Request could not be completed.");
        return "redirect:/item";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception exception, HttpServletRequest request) {
        log.log(Level.SEVERE, "Unexpected request failure.", exception);
        RequestContextUtils.getOutputFlashMap(request)
            .put("errorMessage", "An unexpected error occurred.");
        return "redirect:/item";
    }
}

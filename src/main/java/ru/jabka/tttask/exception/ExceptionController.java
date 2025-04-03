package ru.jabka.tttask.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.jabka.tttask.model.ServiceResponse;

@Log4j2
@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceResponse> handleException(Exception e) {
        log.error(e);
        return ResponseEntity.internalServerError()
                .body(ServiceResponse.builder()
                        .success(false)
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ServiceResponse> handleBadRequestException(BadRequestException e) {
        log.error(e);
        return ResponseEntity.badRequest()
                .body(new ServiceResponse(false, e.getMessage()));
    }
}
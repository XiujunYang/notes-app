package com.example.notesapp.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Log4j2
public class GlobalRestExceptionHandler {

    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMediaTypeNotAcceptableException.class,
            UnsatisfiedServletRequestParameterException.class,
            MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class,
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class})
    public ResponseEntity<String> handleInvalidException(Exception ex) {
        log.error("handleInvalidException:", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<String> handleANoteNotFoundException(NoteNotFoundException ex) {
        log.error("handleANoteNotFoundException:", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<String> handleConcurrencyFailureException(ConcurrencyFailureException ex) {
        log.error("handleConcurrencyFailureException:", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body("there is data conflict, please try it again");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllException(Exception ex) {
        log.error("handleAllException:", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error：" + ex.getMessage());
    }
}

package raf.rs.raf_cloud_backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import raf.rs.raf_cloud_backend.dto.ErrorResponse;
import raf.rs.raf_cloud_backend.service.WebSocketBroadcaster;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private WebSocketBroadcaster webSocketBroadcaster;


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException e,
            WebRequest request) {

        logger.error("ResourceNotFoundException: " + e.getMessage());

        webSocketBroadcaster.notifyWarning(
                "Resource not found: " + e.getMessage(),
                null
        );

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                e.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperationException(
            InvalidOperationException e,
            WebRequest request) {

        logger.error("InvalidOperationException: " + e.getMessage());

        webSocketBroadcaster.notifyError(
                "INVALID_OPERATION",
                e.getMessage(),
                null
        );

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Operation",
                e.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException e,
            WebRequest request) {

        logger.error("ValidationException: " + e.getMessage());

        webSocketBroadcaster.notifyWarning(
                "Validation error: " + e.getMessage(),
                null
        );

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Validation Error",
                e.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            WebRequest request) {

        logger.error("Validation Error: ");

        BindingResult bindingResult = e.getBindingResult();
        List<String> details = new ArrayList<>();

        bindingResult.getFieldErrors().forEach(error ->
                details.add(error.getField() + ": " + error.getDefaultMessage())
        );

        bindingResult.getGlobalErrors().forEach(error ->
                details.add(error.getObjectName() + ": " + error.getDefaultMessage())
        );

        webSocketBroadcaster.notifyWarning(
                "Validation failed: " + String.join("; ", details),
                null
        );

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Input validation error",
                request.getDescription(false).replace("uri=", ""),
                details
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception e,
            WebRequest request) {

        logger.error("Internal Server Error: " + e.getMessage(), e);

        webSocketBroadcaster.notifyError(
                "INTERNAL_SERVER_ERROR",
                e.getMessage(),
                null
        );

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}



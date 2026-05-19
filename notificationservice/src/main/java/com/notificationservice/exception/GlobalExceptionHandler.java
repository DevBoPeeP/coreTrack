package com.notificationservice.exception;

import com.notificationservice.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.kafka.KafkaException;
import org.springframework.messaging.MessagingException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ══════════════════════════════════════════════════════════════════
    // 400 — VALIDATION ERRORS
    // ══════════════════════════════════════════════════════════════════

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("action=handle_exception type=MethodArgumentNotValidException errors={}", errors);
        return ResponseEntity.badRequest().body(ApiResponse.error("01", "Validation failed", HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, b) -> a
                ));
        log.warn("action=handle_exception type=ConstraintViolationException errors={}", errors);
        return ResponseEntity.badRequest().body(ApiResponse.error("01", "Constraint violation", HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        log.warn("action=handle_exception type=HttpMessageNotReadableException message={}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error("01", "Malformed or missing request body: " + ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' must be of type '%s' but received '%s'",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown", ex.getValue());
        log.warn("action=handle_exception type=MethodArgumentTypeMismatchException message={}", message);
        return ResponseEntity.badRequest().body(ApiResponse.error("01", message, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<String>> handleMissingParam(MissingServletRequestParameterException ex) {
        String message = String.format("Required parameter '%s' of type '%s' is missing", ex.getParameterName(), ex.getParameterType());
        log.warn("action=handle_exception type=MissingServletRequestParameterException message={}", message);
        return ResponseEntity.badRequest().body(ApiResponse.error("01", message, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiResponse<String>> handleMissingPathVariable(MissingPathVariableException ex) {
        log.warn("action=handle_exception type=MissingPathVariableException variable={}", ex.getVariableName());
        return ResponseEntity.badRequest().body(ApiResponse.error("01", "Missing path variable: " + ex.getVariableName(), HttpStatus.BAD_REQUEST));
    }

    // ══════════════════════════════════════════════════════════════════
    // 401 & 403 — SECURITY
    // ══════════════════════════════════════════════════════════════════

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("action=handle_exception type=AuthenticationException message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("02", "Authentication failed: " + ex.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("action=handle_exception type=AccessDeniedException message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("03", "Access denied: you do not have permission", HttpStatus.FORBIDDEN));
    }

    // ══════════════════════════════════════════════════════════════════
    // 404 — NOT FOUND
    // ══════════════════════════════════════════════════════════════════

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNoHandlerFound(NoHandlerFoundException ex) {
        log.warn("action=handle_exception type=NoHandlerFoundException url={}", ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("04", "Endpoint not found: " + ex.getRequestURL(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("action=handle_exception type=ResourceNotFoundException message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("04", ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    // ══════════════════════════════════════════════════════════════════
    // 405, 409, 413, 415 — HTTP PROTOCOL & CONFLICTS
    // ══════════════════════════════════════════════════════════════════

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        String supported = ex.getSupportedMethods() != null ? String.join(", ", ex.getSupportedMethods()) : "unknown";
        log.warn("action=handle_exception type=HttpRequestMethodNotSupportedException method={}", ex.getMethod());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("05", "HTTP method '" + ex.getMethod() + "' not supported. Supported: " + supported, HttpStatus.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<String>> handleDuplicateKey(DuplicateKeyException ex) {
        log.warn("action=handle_exception type=DuplicateKeyException message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("06", "Resource already exists: duplicate key violation", HttpStatus.CONFLICT));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<String>> handleConflict(ConflictException ex) {
        log.warn("action=handle_exception type=ConflictException message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error("06", ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<String>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("action=handle_exception type=MaxUploadSizeExceededException message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ApiResponse.error("07", "File size exceeds the maximum allowed limit", HttpStatus.PAYLOAD_TOO_LARGE));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        log.warn("action=handle_exception type=HttpMediaTypeNotSupportedException contentType={}", ex.getContentType());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error("08", "Unsupported media type: " + ex.getContentType() + ". Use application/json", HttpStatus.UNSUPPORTED_MEDIA_TYPE));
    }

    // ══════════════════════════════════════════════════════════════════
    // 500 — SYSTEM FAILURES (DB, KAFKA, EXTERNAL APIs)
    // ══════════════════════════════════════════════════════════════════

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("action=handle_exception type=DataIntegrityViolationException message={}", ex.getMessage());
        String message = ex.getMessage() != null && ex.getMessage().contains("Duplicate entry")
                ? "A record with the same unique value already exists"
                : "Database constraint violation";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("98", message, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<String>> handleDataAccessException(DataAccessException ex) {
        log.error("action=handle_exception type=DataAccessException", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("98", "Database error — please try again later", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(KafkaException.class)
    public ResponseEntity<ApiResponse<String>> handleKafkaException(KafkaException ex) {
        log.error("action=handle_exception type=KafkaException", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("97", "Event streaming error — please try again later", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ApiResponse<String>> handleMessagingException(MessagingException ex) {
        log.error("action=handle_exception type=MessagingException", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("96", "Real-time notification delivery failed", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<ApiResponse<String>> handleConnectException(ConnectException ex) {
        log.error("action=handle_exception type=ConnectException", ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiResponse.error("95", "External service is unavailable", HttpStatus.SERVICE_UNAVAILABLE));
    }

    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<ApiResponse<String>> handleSocketTimeout(SocketTimeoutException ex) {
        log.error("action=handle_exception type=SocketTimeoutException", ex);
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(ApiResponse.error("94", "External service request timed out", HttpStatus.GATEWAY_TIMEOUT));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<String>> handleExternalServiceException(ExternalServiceException ex) {
        log.error("action=handle_exception type=ExternalServiceException service={} error={}", ex.getServiceName(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ApiResponse.error("93", "Error communicating with " + ex.getServiceName() + ": " + ex.getMessage(), HttpStatus.BAD_GATEWAY));
    }

    @ExceptionHandler(PushNotificationException.class)
    public ResponseEntity<ApiResponse<String>> handlePushNotificationException(PushNotificationException ex) {
        log.error("action=handle_exception type=PushNotificationException error={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("92", "Push notification delivery failed: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ApiResponse<String>> handleEmailDeliveryException(EmailDeliveryException ex) {
        log.error("action=handle_exception type=EmailDeliveryException recipient={} error={}", ex.getRecipient(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("91", "Email delivery failed: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("action=handle_exception type=IllegalArgumentException message={}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error("01", "Invalid argument: " + ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    // ══════════════════════════════════════════════════════════════════
    // 500 — CATCH-ALL FALLBACK
    // ══════════════════════════════════════════════════════════════════

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleAllExceptions(Exception ex) {
        log.error("action=handle_exception type=UnhandledException class={} message={}", ex.getClass().getName(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("99", "An unexpected error occurred — please try again later", HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
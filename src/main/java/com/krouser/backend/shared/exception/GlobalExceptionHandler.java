package com.krouser.backend.shared.exception;

import com.krouser.backend.shared.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.krouser.backend.audit.service.AuditService;
import com.krouser.backend.audit.entity.AuditEvent;
import com.krouser.backend.audit.util.AuditDetailsBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private final AuditService auditService;

        public GlobalExceptionHandler(AuditService auditService) {
                this.auditService = auditService;
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiError> handleBusinessException(BusinessException ex, HttpServletRequest request) {
                ApiError apiError = new ApiError(
                                ex.getStatus().value(),
                                ex.getStatus().getReasonPhrase(),
                                ex.getMessage(),
                                null,
                                request.getRequestURI());

                return new ResponseEntity<>(apiError, ex.getStatus());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                List<String> details = new ArrayList<>();
                for (FieldError error : ex.getBindingResult().getFieldErrors()) {
                        details.add(error.getField() + ": " + error.getDefaultMessage());
                }
                ApiError apiError = new ApiError(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                "Fallo en la validación",
                                details,
                                request.getRequestURI());

                auditService.audit("VALIDATION_FAIL", "VALIDATION", AuditEvent.AuditOutcome.FAIL,
                                null, getCurrentUsername(), "Unknown", null,
                                new AuditDetailsBuilder().add("errors", details.toString()).build());

                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
                ApiError apiError = new ApiError(
                                HttpStatus.UNAUTHORIZED.value(),
                                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                "Credenciales inválidas o cuenta no activa",
                                null,
                                request.getRequestURI());

                auditService.audit("AUTH_FAIL_BAD_CREDENTIALS", "AUTH", AuditEvent.AuditOutcome.FAIL,
                                null, "anonymous", null, null, null);

                return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(DisabledException.class)
        public ResponseEntity<ApiError> handleDisabledException(DisabledException ex, HttpServletRequest request) {
                ApiError apiError = new ApiError(
                                HttpStatus.FORBIDDEN.value(),
                                HttpStatus.FORBIDDEN.getReasonPhrase(),
                                "Cuenta desactivada. Verifique su correo.",
                                null,
                                request.getRequestURI());

                return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
                ApiError apiError = new ApiError(
                                HttpStatus.FORBIDDEN.value(),
                                HttpStatus.FORBIDDEN.getReasonPhrase(),
                                "Acceso Denegado",
                                List.of(ex.getMessage()),
                                request.getRequestURI());

                auditService.audit("ACCESS_DENIED_HANDLER", "ACCESS_CONTROL", AuditEvent.AuditOutcome.FAIL,
                                null, getCurrentUsername(), null, null,
                                new AuditDetailsBuilder().add("message", ex.getMessage()).build());

                return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex,
                        HttpServletRequest request) {
                ApiError apiError = new ApiError(
                                HttpStatus.NOT_FOUND.value(),
                                HttpStatus.NOT_FOUND.getReasonPhrase(),
                                ex.getMessage(),
                                null,
                                request.getRequestURI());

                auditService.audit("RESOURCE_NOT_FOUND", "VALIDATION", AuditEvent.AuditOutcome.FAIL,
                                null, getCurrentUsername(), ex.getResourceType(), ex.getResourceIdentifier(),
                                new AuditDetailsBuilder().add("message", ex.getMessage()).build());

                return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ApiError> handleDuplicateResource(DuplicateResourceException ex,
                        HttpServletRequest request) {
                ApiError apiError = new ApiError(
                                HttpStatus.CONFLICT.value(),
                                HttpStatus.CONFLICT.getReasonPhrase(),
                                ex.getMessage(),
                                null,
                                request.getRequestURI());

                auditService.audit("DUPLICATE_RESOURCE", "VALIDATION", AuditEvent.AuditOutcome.FAIL,
                                null, getCurrentUsername(), ex.getResourceType(), ex.getResourceIdentifier(),
                                new AuditDetailsBuilder().add("message", ex.getMessage()).build());

                return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(ResourceInUseException.class)
        public ResponseEntity<ApiError> handleResourceInUse(ResourceInUseException ex, HttpServletRequest request) {
                ApiError apiError = new ApiError(
                                HttpStatus.CONFLICT.value(),
                                HttpStatus.CONFLICT.getReasonPhrase(),
                                ex.getMessage(),
                                List.of("El recurso está en uso y no puede ser eliminado"),
                                request.getRequestURI());

                auditService.audit("RESOURCE_IN_USE", "VALIDATION", AuditEvent.AuditOutcome.FAIL,
                                null, getCurrentUsername(), ex.getResourceType(), ex.getResourceIdentifier(),
                                new AuditDetailsBuilder()
                                                .add("message", ex.getMessage())
                                                .add("usedBy", ex.getUsedBy())
                                                .build());

                return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
                ApiError apiError = new ApiError(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                ex.getMessage(),
                                null,
                                request.getRequestURI());

                auditService.audit("INVALID_ARGUMENT", "VALIDATION", AuditEvent.AuditOutcome.FAIL,
                                null, getCurrentUsername(), null, null,
                                new AuditDetailsBuilder().add("message", ex.getMessage()).build());

                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGlobalException(Exception ex, HttpServletRequest request) {
                ApiError apiError = new ApiError(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                "Ocurrió un error interno inesperado",
                                null,
                                request.getRequestURI());

                ex.printStackTrace();

                auditService.audit("UNHANDLED_EXCEPTION", "SYSTEM", AuditEvent.AuditOutcome.FAIL,
                                null, getCurrentUsername(), null, null,
                                new AuditDetailsBuilder().add("message", ex.getMessage()).build());

                return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        private String getCurrentUsername() {
                try {
                        return SecurityContextHolder.getContext().getAuthentication().getName();
                } catch (Exception e) {
                        return "anonymous";
                }
        }
}

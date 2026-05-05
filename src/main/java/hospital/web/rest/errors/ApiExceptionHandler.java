package hospital.web.rest.errors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> details = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.add(
                Map.of(
                    "field",
                    error.getField(),
                    "message",
                    error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage()
                )
            );
        }
        return ResponseEntity.badRequest().body(error("Invalid input", "INVALID_INPUT", details));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Invalid credentials", "INVALID_CREDENTIALS", null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error("Forbidden", "FORBIDDEN", null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(error(ex.getMessage(), "INVALID_INPUT", null));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage();
        if (message.toLowerCase().contains("unauthorized")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Unauthorized", "UNAUTHORIZED", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(message, "NOT_FOUND", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("Something went wrong", "INTERNAL_ERROR", null));
    }

    private Map<String, Object> error(String message, String code, List<Map<String, String>> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        body.put("code", code);
        if (details != null) {
            body.put("details", details);
        }
        return body;
    }
}

package Exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;


@ControllerAdvice
public class ExceptionResponseHandler {


    @ExceptionHandler(ExceptionAuthorization.class)
    public ResponseEntity<Error> handleExceptionAuthorization(ExceptionAuthorization exception) {
        return buildErrorResponse(exception, HttpStatus.UNAUTHORIZED, null, exception.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Error> handleRuntimeException(RuntimeException exception, WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST, request, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Error> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST, request, exception.getMessage());
    }
    @ExceptionHandler(ExceptionResponse.class)
    public ResponseEntity<Error> handleExceptionResponse(ExceptionResponse exception, WebRequest request) {
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST, request, exception.getMessage());
    }

    private ResponseEntity<Error> buildErrorResponse(Exception exception, HttpStatus status, WebRequest request, String message) {
        Error errorDto = new Error(status.value(), message);
        return ResponseEntity.status(status).body(errorDto);
    }


    public static class Error {
        private final int status;
        private final String message;

        public Error(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}

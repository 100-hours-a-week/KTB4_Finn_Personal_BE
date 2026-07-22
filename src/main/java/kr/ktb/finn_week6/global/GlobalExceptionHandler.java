package kr.ktb.finn_week6.global;


import kr.ktb.finn_week6.global.customException.*;
import kr.ktb.finn_week6.global.dto.ApiResponse;
import kr.ktb.finn_week6.global.dto.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(e.getMessage(),null));
    }

    //400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e){
        String message = e.getBindingResult()
                .getFieldErrors()
                .getFirst()
                .getDefaultMessage();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(message,null));
    }

    @ExceptionHandler(CurrentPasswordMismatch.class)
    public ResponseEntity<ApiResponse<Void>> handleCurrentPasswordMismatch(CurrentPasswordMismatch e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(e.getMessage(),null));
    }

    //401
    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(UnauthenticatedException e){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(e.getMessage(),null));
    }
    @ExceptionHandler(AuthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorizedException(AuthorizedException e){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(e.getMessage(),null));
    }
    //403
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(ForbiddenException e){
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(e.getMessage(),null));
    }
    //404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(NotFoundException e){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(e.getMessage(),null));
    }
    //409
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmailException(DuplicateEmailException e){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(e.getMessage(),null));
    }

    //401
    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidLoginException(IncorrectPasswordException e){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(e.getMessage(),null));
    }
    @ExceptionHandler(NoSuchEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchEmailException(NoSuchEmailException e){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(e.getMessage(),null));
    }


    @ExceptionHandler(IllegalResourceStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalResourceStateException(IllegalResourceStateException e){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(e.getMessage(),null));
    }

    //500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("Internal server error", null));
    }
}

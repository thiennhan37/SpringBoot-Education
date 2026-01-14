package com.example.demo_database.exception;

import com.example.demo_database.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// xử lí các Exception ở 1 khu vực tập trung thay vì try-catch rải rác trong các controller
//


@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException exception){
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.UNCATEGORIZED_EXCEPTION));
    }
    @ExceptionHandler(value = MyAppException.class)
    ResponseEntity<ApiResponse> handlingMyAppException(MyAppException exception){
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(exception.getErrorCode()));
    }
    //Exception cho các ràng buộc trong request
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception){
        String enumKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.KEY_INVALID;
        try{
            errorCode = ErrorCode.valueOf(enumKey);
        } catch(IllegalArgumentException exception1){

        }
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }
}

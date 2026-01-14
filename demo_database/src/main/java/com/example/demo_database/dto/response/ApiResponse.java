package com.example.demo_database.dto.response;

import com.example.demo_database.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// custom thành json rõ ràng cho FE, FE chỉ cần quan tâm đến error-code để xử lí
// BE chỉ cần quan tâm xử lí logic business, có thể mở rộng thêm Attribute mà ko ảnh hưởng FE


//@JsonInclude(JsonInclude.Include.NON_NULL)
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class ApiResponse<T> {
    public int code = 1000;
    public String message = "Successful";
    public T result;

    public static ApiResponse<?> error(ErrorCode errorCode){
        return ApiResponse.builder().code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }
    public static <T> ApiResponse<T> success(T result){
        return ApiResponse.<T>builder()
                .result(result)
                .build();
    }
}

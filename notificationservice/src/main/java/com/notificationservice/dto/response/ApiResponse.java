package com.notificationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String responseCode;
    private String responseMessage;
    private HttpStatus httpStatus;
    private T data;

    // --- Factory Methods for Cleaner Code ---

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .responseCode("00")
                .responseMessage("Success")
                .httpStatus(HttpStatus.OK)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .responseCode("00")
                .responseMessage(message)
                .httpStatus(HttpStatus.OK)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, HttpStatus status) {
        return ApiResponse.<T>builder()
                .responseCode(code)
                .responseMessage(message)
                .httpStatus(status)
                .data(null)
                .build();
    }
}
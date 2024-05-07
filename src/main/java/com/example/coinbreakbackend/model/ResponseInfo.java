package com.example.coinbreakbackend.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ResponseInfo {
    private Object data;
    private String httpCode;
    private String stackTrace;
    private String info;
}

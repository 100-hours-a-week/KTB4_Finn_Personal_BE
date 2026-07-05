package kr.ktb.finn_week6.global.dto;

public record ApiResponse<T> (String message, T data){

}

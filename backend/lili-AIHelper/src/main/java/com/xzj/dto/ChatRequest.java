package com.xzj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "conversationId 不能为空") String conversationId,
        @NotBlank(message = "message 不能为空") @Size(max = 2000, message = "单次提问不要超过 2000 字") String message) {
}
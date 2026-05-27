package com.ticketmanagement.ticket.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddWorklogRequest(
        @NotNull
        LocalDate workDate,

        @NotNull
        @Min(1)
        @Max(1440)
        Integer durationMinutes,

        @NotBlank
        @Size(max = 2000)
        String description) {
}

package com.zhinengpt.campuscatering.seckill.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SeckillActivitySaveRequest {

    private Long id;

    @NotNull
    private Long storeId;

    @NotNull
    private Long skuId;

    @NotBlank
    @Size(max = 64)
    private String activityName;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal seckillPrice;

    @NotNull
    @Min(1)
    private Integer stock;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer status = 1;
}

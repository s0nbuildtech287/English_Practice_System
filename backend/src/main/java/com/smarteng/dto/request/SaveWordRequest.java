package com.smarteng.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveWordRequest {
    @NotBlank(message = "Từ vựng không được để trống")
    private String word;
    private String ipa;
    private String meaningVi;
    private String meaningEn;
    private String audioUrl;
    private String contextSentence;
}

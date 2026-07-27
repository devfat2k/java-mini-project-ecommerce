package com.devfat.mini_ecommerce.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequestDto {
    private String to;
    private String subject;
    private String messageBody;
//    private String attachmentPath;
}
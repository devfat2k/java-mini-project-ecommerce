package com.devfat.mini_ecommerce.notification.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequestDto {
    private String to;
    private String subject;
    private String messageBody;
}

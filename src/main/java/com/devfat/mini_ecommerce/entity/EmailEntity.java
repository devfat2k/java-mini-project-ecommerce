package com.devfat.mini_ecommerce.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailEntity {
    private String to;
    private String subject;
    private String messageBody;
    private String attachmentPath;
}
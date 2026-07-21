package com.devfat.mini_ecommerce.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;        // Danh sách dữ liệu của trang hiện tại
    private int page;               // Trang hiện tại (0-indexed hoặc tùy chỉnh)
    private int size;               // Kích thước trang (số bản ghi tối đa trên trang)
    private long totalElements;     // Tổng số bản ghi trong database
    private int totalPages;         // Tổng số trang
    private boolean last;           // Đây có phải là trang cuối cùng không?

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}

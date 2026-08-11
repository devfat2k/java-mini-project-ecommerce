package com.devfat.mini_ecommerce.shared.base;












import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class BasePageRequest {
    private int page;
    private int size;
    private String sortBy = "id";
    private String direction = "asc";

    public Pageable toPageable() {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortBy) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}

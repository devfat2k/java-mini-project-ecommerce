package com.devfat.mini_ecommerce.shared.util;

import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.ArrayList;
import java.util.List;

public class CustomPageableArgumentResolver implements HandlerMethodArgumentResolver {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT = "id";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Pageable.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        PageableDefault pageableDefault = parameter.getParameterAnnotation(PageableDefault.class);

        int fallbackPage = (pageableDefault != null) ? pageableDefault.page() : DEFAULT_PAGE;
        int fallbackSize = (pageableDefault != null) ? pageableDefault.size() : DEFAULT_SIZE;

        int page = parseInt(webRequest.getParameter("page"), fallbackPage);
        int size = parseInt(webRequest.getParameter("size"), fallbackSize);

        String[] sortParams = webRequest.getParameterValues("sort");
        String directionParam = webRequest.getParameter("direction");

        Sort sort;

        if (sortParams != null && sortParams.length > 0 && hasNonBlankString(sortParams)) {
            List<Sort.Order> orders = new ArrayList<>();
            for (String sortParam : sortParams) {
                if (sortParam == null || sortParam.isBlank()) {
                    continue;
                }
                String[] parts = sortParam.split(",");
                String property = parts[0].trim();
                Sort.Direction direction;

                if (parts.length > 1 && !parts[1].isBlank()) {
                    direction = parseDirection(parts[1].trim(), Sort.Direction.ASC);
                } else if (directionParam != null && !directionParam.isBlank()) {
                    direction = parseDirection(directionParam.trim(), Sort.Direction.ASC);
                } else if (pageableDefault != null) {
                    direction = pageableDefault.direction();
                } else {
                    direction = Sort.Direction.ASC;
                }

                orders.add(new Sort.Order(direction, property));
            }
            sort = orders.isEmpty() ? getDefaultSort(pageableDefault) : Sort.by(orders);
        } else {
            sort = getDefaultSort(pageableDefault);
        }

        return PageRequest.of(page, size, sort);
    }

    private boolean hasNonBlankString(String[] array) {
        for (String s : array) {
            if (s != null && !s.isBlank()) {
                return true;
            }
        }
        return false;
    }

    private Sort getDefaultSort(PageableDefault pageableDefault) {
        if (pageableDefault != null && pageableDefault.sort().length > 0 && !pageableDefault.sort()[0].isBlank()) {
            List<Sort.Order> orders = new ArrayList<>();
            for (String prop : pageableDefault.sort()) {
                if (!prop.isBlank()) {
                    orders.add(new Sort.Order(pageableDefault.direction(), prop.trim()));
                }
            }
            if (!orders.isEmpty()) {
                return Sort.by(orders);
            }
        }
        return Sort.by(Sort.Direction.ASC, DEFAULT_SORT);
    }

    private Sort.Direction parseDirection(String val, Sort.Direction defaultDir) {
        if ("desc".equalsIgnoreCase(val)) {
            return Sort.Direction.DESC;
        } else if ("asc".equalsIgnoreCase(val)) {
            return Sort.Direction.ASC;
        }
        return defaultDir;
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

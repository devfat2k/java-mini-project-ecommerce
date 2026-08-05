package com.devfat.mini_ecommerce.shared.util;











import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CustomPageableArgumentResolver implements HandlerMethodArgumentResolver {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT = "id";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Pageable.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        int page = parseInt(webRequest.getParameter("page"), DEFAULT_PAGE);
        int size = parseInt(webRequest.getParameter("size"), DEFAULT_SIZE);

        String sort = webRequest.getParameter("sort");
        String direction = webRequest.getParameter("direction");

        String sortField = (sort == null || sort.isBlank()) ? DEFAULT_SORT : sort;
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(dir, sortField));
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

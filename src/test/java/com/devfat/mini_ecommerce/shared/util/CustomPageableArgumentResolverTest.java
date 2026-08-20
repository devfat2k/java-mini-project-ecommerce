package com.devfat.mini_ecommerce.shared.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.context.request.NativeWebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomPageableArgumentResolverTest {

    private CustomPageableArgumentResolver resolver;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private MethodParameter methodParameterWithDefault;

    @BeforeEach
    void setUp() {
        resolver = new CustomPageableArgumentResolver();
    }

    @Test
    @DisplayName("Should support Pageable parameter type")
    void shouldSupportPageable() {
        doReturn(Pageable.class).when(methodParameterWithDefault).getParameterType();
        assertTrue(resolver.supportsParameter(methodParameterWithDefault));
    }

    @Test
    @DisplayName("Should use @PageableDefault fallback when sort param is missing")
    void shouldUsePageableDefaultFallback() throws Exception {
        PageableDefault mockDefault = DummyController.class
                .getMethod("endpointWithDefault", Pageable.class)
                .getParameters()[0]
                .getAnnotation(PageableDefault.class);

        when(methodParameterWithDefault.getParameterAnnotation(PageableDefault.class)).thenReturn(mockDefault);
        when(webRequest.getParameter("page")).thenReturn("0");
        when(webRequest.getParameter("size")).thenReturn("1");
        when(webRequest.getParameterValues("sort")).thenReturn(null);

        Pageable pageable = (Pageable) resolver.resolveArgument(methodParameterWithDefault, null, webRequest, null);

        assertNotNull(pageable);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(1, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "createdAt"), pageable.getSort());
    }

    @Test
    @DisplayName("Should parse comma-separated sort param like sort=price,asc")
    void shouldParseCommaSeparatedSortParam() throws Exception {
        when(methodParameterWithDefault.getParameterAnnotation(PageableDefault.class)).thenReturn(null);
        when(webRequest.getParameter("page")).thenReturn("0");
        when(webRequest.getParameter("size")).thenReturn("5");
        when(webRequest.getParameterValues("sort")).thenReturn(new String[]{"price,asc"});

        Pageable pageable = (Pageable) resolver.resolveArgument(methodParameterWithDefault, null, webRequest, null);

        assertNotNull(pageable);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.ASC, "price"), pageable.getSort());
    }

    static class DummyController {
        public void endpointWithDefault(
                @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
        ) {}
    }
}

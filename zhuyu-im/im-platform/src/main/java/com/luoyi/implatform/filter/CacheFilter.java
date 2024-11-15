package com.luoyi.implatform.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 缓存过滤器，过滤xss、sql注入等
 */
@Component
@ServletComponentScan
@WebFilter(urlPatterns = "/*", filterName = "xssFilter")
public class CacheFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new CacheHttpServletRequestWrapper((HttpServletRequest) request), response);
    }

}
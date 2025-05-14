package com.vietinbank.kconsumer.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;

public class LanguageUtils {
    public static String getCurrentLanguage() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String acceptLanguage = request.getHeader("Accept-Language");
            
            if (acceptLanguage != null && acceptLanguage.toLowerCase().startsWith("vi")) {
                return "vi";
            }
        }
        return "en"; // Mặc định là tiếng Anh
    }
} 
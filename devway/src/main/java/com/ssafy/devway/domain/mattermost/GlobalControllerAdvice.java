package com.ssafy.devway.domain.mattermost;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Enumeration;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    private final NotificationManager notificationManager;

    @ExceptionHandler(Exception.class)
    public ResponseEntity exceptionTest(Exception e, HttpServletRequest req) {
        e.printStackTrace();
        notificationManager.sendNotification(e, req.getRequestURI(), getParams(req));

        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String getParams(HttpServletRequest req) {
        StringBuilder params = new StringBuilder();
        Enumeration<String> keys = req.getParameterNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            params.append("- ").append(key).append(" : ").append(req.getParameter(key)).append('\n');
        }

        return params.toString();
    }
}

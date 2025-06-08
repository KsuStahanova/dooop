package com.example.project3.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object exception = request.getAttribute("javax.servlet.error.exception");
        Object message = request.getAttribute("javax.servlet.error.message");

        log.error("Error occurred - Status: {}, Exception: {}, Message: {}", status, exception, message);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            switch (statusCode) {
                case 404:
                    model.addAttribute("error", "Страница не найдена (404)");
                    break;
                case 403:
                    model.addAttribute("error", "Доступ запрещен (403)");
                    break;
                case 500:
                    model.addAttribute("error", "Внутренняя ошибка сервера (500)");
                    break;
                default:
                    model.addAttribute("error", "Произошла ошибка (" + statusCode + ")");
                    break;
            }
        } else {
            model.addAttribute("error", "Неизвестная ошибка");
        }

        if (exception != null) {
            log.error("Exception details: ", (Exception) exception);
        }

        return "error";
    }
}
package com.example.kmeansspringboot.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CustomErrorController implements ErrorController {


    @RequestMapping("/error")
    @GetMapping("/error")
    @ResponseBody
    String error(HttpServletRequest request) {
        return "Si è verificato un errore, torna alla schermata precedente e riprova";
    }
}
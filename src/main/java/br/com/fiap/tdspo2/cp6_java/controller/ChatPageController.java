package br.com.fiap.tdspo2.cp6_java.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatPageController {

    @GetMapping("/")
    public String chat() {
        return "chat";
    }
}
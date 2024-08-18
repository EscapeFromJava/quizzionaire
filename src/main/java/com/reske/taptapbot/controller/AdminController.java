package com.reske.taptapbot.controller;

import com.reske.taptapbot.model.Session;
import com.reske.taptapbot.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SessionService sessionService;

    @GetMapping("/sessions")
    public Map<Long, Session> getSessions() {
        return sessionService.getSessions();
    }

}

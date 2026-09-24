package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.common.Result;
import org.example.entity.StudySession;
import org.example.entity.Word;
import org.example.service.StudyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @PostMapping("/start")
    public Result<StudySession> start(@RequestParam Integer userId) {
        return Result.success(studyService.startStudy(userId));
    }

    @PostMapping("/answer")
    public Result<Void> answer(
            @RequestParam Integer userId,
            @RequestParam Integer sessionId,
            @RequestParam Integer wordId,
            @RequestParam String action,
            @RequestParam(required = false) Integer latencyMs) {
        studyService.submitAnswer(userId, sessionId, wordId, action, latencyMs);
        return Result.success();
    }

    @PostMapping("/end")
    public Result<StudySession> end(@RequestParam Integer sessionId) {
        return Result.success(studyService.endStudy(sessionId));
    }

    @GetMapping("/next")
    public Result<List<Word>> next(@RequestParam Integer userId, @RequestParam Integer sessionId) {
        return Result.success(studyService.getNextGroup(userId, sessionId));
    }
}

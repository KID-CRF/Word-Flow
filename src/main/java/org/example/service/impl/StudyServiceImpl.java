package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.entity.AnswerEvent;
import org.example.entity.StudySession;
import org.example.entity.UserWordState;
import org.example.entity.Word;
import org.example.mapper.StudySessionMapper;
import org.example.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StudyServiceImpl
        extends ServiceImpl<StudySessionMapper, StudySession>
        implements StudyService {

    private final AnswerEventService answerEventService;
    private final UserWordStateService userWordStateService;
    private final WordService wordService;

    @Override
    public StudySession startStudy(Integer userId) {
        StudySession session = new StudySession();
        session.setUserId(userId);
        session.setStartTime(LocalDateTime.now());
        session.setStatus("ongoing");
        session.setTotalWords(0);
        session.setCorrectCount(0);
        session.setWrongCount(0);
        save(session);
        return session;
    }


    @Transactional
    @Override
    public void submitAnswer(Integer userId, Integer sessionId, Integer wordId, Integer round, String action, Integer latencyMs) {

        // 幂等判断
        long count = answerEventService.count(
                new QueryWrapper<AnswerEvent>()
                        .eq("session_id", sessionId)
                        .eq("word_id", wordId)
                        .eq("round", round)
        );
        if (count > 0) {
            return;
        }

        // 1. 写入答题事件
        AnswerEvent event = new AnswerEvent();
        event.setUserId(userId);
        event.setSessionId(sessionId);
        event.setWordId(wordId);
        event.setRound(round);
        event.setAction(action);
        event.setLatencyMs(latencyMs);
        event.setCreatedAt(LocalDateTime.now());
        answerEventService.save(event);

        // 2. 更新用户单词状态
        UserWordState state = userWordStateService.getOne(
                new QueryWrapper<UserWordState>()
                        .eq("user_id", userId)
                        .eq("word_id", wordId)
        );

        if (state == null) {
            state = new UserWordState();
            state.setUserId(userId);
            state.setWordId(wordId);
            state.setRounds(0);
            state.setCorrectCount(0);
            state.setWrongCount(0);
            state.setLevel(0);
            state.setIsMastered(0);
        }

        state.setRounds(state.getRounds() + 1);
        state.setLastSeenAt(LocalDateTime.now());

        if ("self_known".equals(action) || "review_known".equals(action)) {
            state.setCorrectCount(state.getCorrectCount() + 1);
        } else {
            state.setWrongCount(state.getWrongCount() + 1);
        }

        userWordStateService.saveOrUpdate(state);
    }

    @Override
    public StudySession endStudy(Integer sessionId) {
        StudySession session = getById(sessionId);
        if (session == null) return null;

        session.setEndTime(LocalDateTime.now());
        session.setStatus("finished");

        // 统计正确/错误次数
        long correct = answerEventService.count(
                new QueryWrapper<AnswerEvent>()
                        .eq("session_id", sessionId)
                        .in("action", "self_known", "review_known")
        );
        long wrong = answerEventService.count(
                new QueryWrapper<AnswerEvent>()
                        .eq("session_id", sessionId)
                        .in("action", "self_unknown", "review_unknown")
        );

        session.setCorrectCount((int) correct);
        session.setWrongCount((int) wrong);
        session.setTotalWords((int) (correct + wrong));

        updateById(session);
        return session;
    }

    private double computeScore(UserWordState s) {
        double wWrong = 2.0;    // 错误权重
        double wRounds = 1.0;   // 轮次权重
        double wCorrect = 0.5;  // 正确权重
        double wTime = 0.5;     // 时间衰减

        long days = s.getLastSeenAt() == null ? 0 :
                ChronoUnit.DAYS.between(s.getLastSeenAt(), LocalDateTime.now());

        int correct = s.getCorrectCount() == null ? 0 : s.getCorrectCount();
        int wrong = s.getWrongCount() == null ? 0 : s.getWrongCount();
        int rounds = s.getRounds() == null ? 0 : s.getRounds();

        return wWrong * wrong
                + wRounds * rounds
                - wCorrect * correct
                - wTime * days;
    }

    @Override
    public List<Word> getNextGroup(Integer userId, Integer sessionId) {
        List<UserWordState> states = userWordStateService.list(
                new QueryWrapper<UserWordState>()
                        .eq("user_id", userId)
                        .eq("is_mastered", 0)
        );

        // 按公式降序排序
        states.sort((a, b) -> Double.compare(computeScore(b), computeScore(a)));

        // 取前 10 个
        List<Integer> wordIds = states.stream()
                .limit(10)
                .map(UserWordState::getWordId)
                .collect(Collectors.toList());

        if (wordIds.isEmpty()) return List.of();

        // 查出单词并按 wordIds 顺序重排
        List<Word> words = wordService.listByIds(wordIds);
        Map<Integer, Word> map = words.stream()
                .collect(Collectors.toMap(Word::getId, w -> w));
        return wordIds.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

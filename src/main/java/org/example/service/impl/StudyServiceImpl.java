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

    @Override
    @Transactional
    public void submitAnswer(Integer userId, Integer sessionId, Integer wordId, String action, Integer latencyMs) {
        // 1. 写入答题事件
        AnswerEvent event = new AnswerEvent();
        event.setUserId(userId);
        event.setSessionId(sessionId);
        event.setWordId(wordId);
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
            state.setCorrectCount(0);
            state.setWrongCount(0);
            state.setRounds(0);
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

    @Override
    public List<Word> getNextGroup(Integer userId, Integer sessionId) {
        // 先简单实现：返回所有未掌握的单词
        List<UserWordState> states = userWordStateService.list(
                new QueryWrapper<UserWordState>()
                        .eq("user_id", userId)
                        .eq("is_mastered", 0)
        );

        //待补充下一组排序逻辑
        List<Integer> wordIds = states.stream()
                .map(UserWordState::getWordId)
                .collect(Collectors.toList());

        if (wordIds.isEmpty()) return List.of();

        return wordService.listByIds(wordIds);
    }
}

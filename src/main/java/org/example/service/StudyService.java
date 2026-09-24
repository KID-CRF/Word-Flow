package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.StudySession;
import org.example.entity.Word;

import java.util.List;

public interface StudyService extends IService<StudySession> {

    // 开始学习，返回会话
    StudySession startStudy(Integer userId);

    // 提交答题
    void submitAnswer(Integer userId, Integer sessionId, Integer wordId, String action, Integer latencyMs);

    // 结束学习，返回统计
    StudySession endStudy(Integer sessionId);

    // 获取下一组单词（先返回全部未掌握的）
    List<Word> getNextGroup(Integer userId, Integer sessionId);
}
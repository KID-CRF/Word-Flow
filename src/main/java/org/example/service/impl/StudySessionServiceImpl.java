package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.StudySession;
import org.example.mapper.StudySessionMapper;
import org.example.service.StudySessionService;
import org.springframework.stereotype.Service;

@Service
public class StudySessionServiceImpl extends ServiceImpl<StudySessionMapper, StudySession> implements StudySessionService {
}



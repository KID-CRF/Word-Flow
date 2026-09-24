package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.AnswerEvent;
import org.example.mapper.AnswerEventMapper;
import org.example.service.AnswerEventService;
import org.springframework.stereotype.Service;

@Service
public class AnswerEventServiceImpl extends ServiceImpl<AnswerEventMapper, AnswerEvent> implements AnswerEventService {
}


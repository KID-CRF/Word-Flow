package org.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.UserWordState;
import org.example.mapper.UserWordStateMapper;
import org.example.service.UserWordStateService;
import org.springframework.stereotype.Service;

@Service
public class UserWordStateServiceImpl extends ServiceImpl<UserWordStateMapper, UserWordState> implements UserWordStateService {
}


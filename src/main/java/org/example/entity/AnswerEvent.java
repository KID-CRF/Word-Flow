package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("answer_event")
public class AnswerEvent {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer wordId;
    private Integer sessionId;
    private Integer round;
    private Integer groupIndex;
    private String action;
    private Integer latencyMs;
    private LocalDateTime createdAt;
}

package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("study_session")
public class StudySession {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Integer totalWords;
    private Integer correctCount;
    private Integer wrongCount;
}

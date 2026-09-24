package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_word_state")
public class UserWordState {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer wordId;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer rounds;
    private LocalDateTime lastSeenAt;
    private Integer level;
    private Integer isMastered;
}

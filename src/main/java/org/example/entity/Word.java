package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("words")
public class Word {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private  String text;
    private String meaning;
    private String tag;
    private Integer frequency;
}

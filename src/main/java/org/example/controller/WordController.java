package org.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.entity.Word;
import org.example.service.WordService;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.common.Result;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @GetMapping("/search")
    public Result<List<Word>> search(@RequestParam String text) {
        QueryWrapper<Word> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.like("text", text).or().like("tag", text));
        return  Result.success(wordService.list(wrapper)) ;
    }


    @PostMapping
    public Result<Word> create(@RequestBody Word word) {
        wordService.save(word);
        return Result.success(word);
    }

    @GetMapping("/{id}")
    public Result<Word> getById(@PathVariable Integer id) {
        return Result.success(wordService.getById(id));
    }

    @GetMapping
    public Result<List<Word>> list() {
        return Result.success(wordService.list());
    }

    @PutMapping
    public Result<Word> update(@RequestBody Word word) {
        wordService.updateById(word);
        return Result.success(word);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        wordService.removeById(id);
        return Result.success();
    }

    @PostMapping("/batch")
    public Result<Integer> batchImport(@RequestBody List<Word> words) {
        wordService.saveBatch(words);
        return Result.success(words.size());
    }

    // 分页查询
    @GetMapping("/page")
    public Result<Page<Word>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Word> page = new Page<>(current, size);
        return Result.success(wordService.page(page));
    }

    // 按标签筛选
    @GetMapping("/tag")
    public Result<List<Word>> getByTag(@RequestParam String tag) {
        QueryWrapper<Word> wrapper = new QueryWrapper<>();
        wrapper.eq("tag", tag);
        return Result.success(wordService.list(wrapper));
    }

    // 按频率排序取前 N
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/top")
    public Result<List<Word>> getTop(@RequestParam(defaultValue = "10") Integer limit) {
        String key = "word:top:" + limit;

        // 1. 查 Redis
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return Result.success((List<Word>) cached);
            }
        } catch (Exception e) {
            System.out.println("Redis 查询失败：" + e.getMessage());
        }

        // 2. 查 MySQL
        QueryWrapper<Word> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("frequency");
        wrapper.last("LIMIT " + limit);
        List<Word> words = wordService.list(wrapper);

        // 3. 写回 Redis
        try {
            redisTemplate.opsForValue().set(key, words, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            System.out.println("Redis 写入失败：" + e.getMessage());
        }

        return Result.success(words);
    }
}

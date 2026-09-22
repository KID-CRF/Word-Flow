package org.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.entity.Word;
import org.example.service.WordService;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

@RestController
@RequestMapping("/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @GetMapping("/search")
    public List<Word> search(@RequestParam String text) {
        QueryWrapper<Word> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.like("text", text).or().like("tag", text));
        return wordService.list(wrapper);
    }


    @PostMapping
    public Word create(@RequestBody Word word) {
        wordService.save(word);
        return word;
    }

    @GetMapping("/{id}")
    public Word getById(@PathVariable Integer id) {
        return wordService.getById(id);
    }

    @GetMapping
    public List<Word> list() {
        return wordService.list();
    }

    @PutMapping
    public Word update(@RequestBody Word word) {
        wordService.updateById(word);
        return word;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        wordService.removeById(id);
    }

    @PostMapping("/batch")
    public int batchImport(@RequestBody List<Word> words) {
        wordService.saveBatch(words);
        return words.size();
    }

    // 分页查询
    @GetMapping("/page")
    public Page<Word> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Word> page = new Page<>(current, size);
        return wordService.page(page);
    }

    // 按标签筛选
    @GetMapping("/tag")
    public List<Word> getByTag(@RequestParam String tag) {
        QueryWrapper<Word> wrapper = new QueryWrapper<>();
        wrapper.eq("tag", tag);
        return wordService.list(wrapper);
    }

    // 按频率排序取前 N
    @GetMapping("/top")
    public List<Word> getTop(@RequestParam(defaultValue = "10") Integer limit) {
        QueryWrapper<Word> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("frequency");
        wrapper.last("LIMIT " + limit);
        return wordService.list(wrapper);
    }
}

package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public List<User> search(@RequestParam String name) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("name", name);
        return userService.list(wrapper);
    }


    @PostMapping
    public User create(@RequestBody User user) {
        userService.save(user);
        return user;
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Integer id) {
        return userService.getById(id);
    }

    @GetMapping
    public List<User> list() {
        return userService.list();
    }

    @PutMapping
    public User update(@RequestBody User user) {
        userService.updateById(user);
        return user;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        userService.removeById(id);
    }
}


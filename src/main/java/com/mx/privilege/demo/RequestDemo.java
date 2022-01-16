package com.mx.privilege.demo;

import com.alibaba.fastjson.JSON;
import com.mx.privilege.annotation.RowPrivilege;
import com.mx.privilege.pojo.UserDto;
import com.mx.privilege.util.RedisUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author mengxu
 * @date 2022/1/16 21:26
 */
@RowPrivilege("name")
@RestController
public class RequestDemo {

    @Resource
    private RedisUtil redisUtil;

    @PostConstruct
    public void init() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("mengxu");
        redisUtil.set("USER:TOKEN:123456", JSON.toJSONString(userDto));
    }


    @PostMapping("/privilege/test")
    public String test(@RequestBody RequestDto requestDto) {
        return "success";
    }
}

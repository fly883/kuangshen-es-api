package com.ldh.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class User {
    //姓名
    private String name;
    //年龄
    private int age;
    //描述
    private String desc;
}

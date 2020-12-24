package com.ldh.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController("AdminIndexController")
@RequestMapping("/es")
public class IndexController {

    @RequestMapping(value = {"/","/index"}, method = RequestMethod.GET)
    public String findBranchDynamics() throws Exception {
        return "index";
    }
}

package com.ldh.controller;

import com.ldh.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController("AdminContentController")
@RequestMapping
public class ContentController {

    @Autowired
    private ContentService contentService;

    //保存数据
    @RequestMapping(value = "/parse/{keyword}", method = RequestMethod.GET)
    public Boolean parse(@PathVariable("keyword") String keyword) throws Exception {
        return contentService.paresContent(keyword);
    }

    //删除数据(通过keyword 删除)
    @RequestMapping(value = "/del/{keyword}", method = RequestMethod.GET)
    public Boolean delete(@PathVariable("keyword") String keyword) throws Exception {
        return contentService.deleteContent(keyword);
    }

    //关键词搜索
    @RequestMapping(value = "/search/{keyword}/{pageNO}/{pageSize}", method = RequestMethod.GET)
    public List<Map<String,Object>> search(@PathVariable("keyword") String keyword,
                                           @PathVariable("pageNO") Integer pageNO,
                                           @PathVariable("pageSize") Integer pageSize) throws Exception {
        if (pageNO==null){ pageNO=1; }
        if (pageSize==null){ pageSize=50; }
        //实现搜索功能
        //return contentService.search(keyword,pageNO,pageSize);
        //实现搜索高亮显示功能
        return contentService.searchHighLight(keyword,pageNO,pageSize);
    }



}

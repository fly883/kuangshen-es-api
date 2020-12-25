package com.ldh.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装页面信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Content {
    //标题
    private String title;
    //图片
    private String img;
    //作者
    private String name;
    //价格
    private String price;
    //出版社
    private String store;
    //商店
    private String shop;
    //日期
    private String date;
    //物流
    private String icons;
    //评论
    private String commit;

}

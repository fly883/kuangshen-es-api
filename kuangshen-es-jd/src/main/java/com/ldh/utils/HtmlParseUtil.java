package com.ldh.utils;

import com.ldh.result.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParseUtil {
    //测试
    /*public static void main(String[] args) throws Exception {
        new HtmlParseUtil().parseDJ("java").forEach(System.out::println);
    }*/

    public List<Content> parseDJ(String keyword ) throws Exception {
        //获取请求: https://search.jd.com/Search?keyword=java
        String url = "https://search.jd.com/Search?keyword=" + keyword;

        //解析页面(Jsoup 返回Document对象 就是浏览器的Document对象（页面对象）)
        Document document = Jsoup.parse(new URL(url), 30000);
        //所有你再js中可以使用的方法，这里都能用!
        Element element = document.getElementById("J_goodsList");
        System.out.println(element.html());
        //获取所有的li元素：<li></li>
        Elements elements = element.getElementsByTag("li");
        //封装结果
        ArrayList<Content> goodsList=new ArrayList<>();

        //获取元素中的内容，这里el 就是每一个li标签
        for (Element el : elements) {
            //关于这种图片特别多的网站，所有图片都是延时加载的!
            //data-lazy-img
            //String img = el.getElementsByTag("").eq(0).attr("src");
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            String name = el.getElementsByClass("p-bi-name").eq(0).text();
            String store = el.getElementsByClass("p-bi-store").eq(0).text();
            String date = el.getElementsByClass("p-bi-date").eq(0).text();
            String shop = el.getElementsByClass("hd-shopname").eq(0).text();
            String icons = el.getElementsByClass("p-icons").eq(0).text();
            String commit = el.getElementsByClass("p-commit").eq(0).text();

            System.out.println("===============================");
            System.out.println(img);
            System.out.println(price);
            System.out.println(title);
            System.out.println(name);
            System.out.println(store);
            System.out.println(date);
            System.out.println(shop);
            System.out.println(icons);
            System.out.println(commit);

            //填充结果
            Content content=new Content();
            content.setTitle(title);
            content.setImg(img);
            content.setPrice(price);
            content.setName(name);
            content.setStore(store);
            content.setDate(date);
            content.setShop(shop);
            content.setIcons(icons);
            content.setCommit(commit);
            goodsList.add(content);
        }
        return goodsList;
    }

}

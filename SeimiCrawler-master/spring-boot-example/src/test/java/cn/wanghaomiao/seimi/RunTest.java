package cn.wanghaomiao.seimi;

import cn.wanghaomiao.seimi.spring.common.CrawlerCache;
import cn.wanghaomiao.seimi.util.X509TrustUtiil;
import org.apache.tools.ant.util.FileUtils;
import org.assertj.core.util.Lists;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author SeimiMaster seimimaster@gmail.com
 * @since 2018/6/5.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RunTest {

    @Test
    //支持分页爬取
    public void pachong3() throws Exception {
        //设置需要下载多少页
        int page = 200;//先爬取10页的内容
        int result = 0;
        for (int i = 1; i <= page; i++) {
            pachong_page("https://qingbuyaohaixiu.com/?page=" + i);
            if (i > page) {
                break;
            }
        }
        System.out.println("爬取结束！一共爬取内容为:" + result * page + "条！");
    }
    public void pachong_page(String url) throws Exception {
        //String url="http://www.ygdy8.net/html/gndy/dyzz/list_23_1.html";
        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla").get();//模拟火狐浏览器
        } catch (IOException e) {
            e.printStackTrace();
        }
        //这里根据在网页中分析的类选择器来获取电影列表所在的节点
        Elements div = doc.getElementsByClass("rr");
        //获取所有详情的链接所在的节点
        Elements tr = doc.getElementsByClass("rcm4");
        //获取列表总数
        int result = tr.size();
        List<String> urls = new ArrayList<String>();
        for (int i = 1; i < tr.size(); i++) {
            Element element = tr.get(i - 1);
//            try {
////                Thread.sleep(1000);//让线程操作不要太快 1秒一次 时间自己设置，主要是模拟人在点击
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            //高清图url
            String aurl = element.select("a").attr("abs:href");
            urls.add(aurl);
            //缩略图url
//            String imgSrc = element.getElementsByTag("amp-img").attr("src");
//            System.out.println("imgSrc = " + imgSrc);
//            String title = element.getElementsByTag("amp-img").attr("alt");
//            System.out.println(title);
//            File file = new File("E:\\test2\\" + title + ".jpg");
//            X509TrustUtiil.downloadFile(imgSrc, file, null);
        }
        getMoore(urls);
    }


    public static void getMoore(List<String> urls) throws Exception {
        for (String url : urls) {
            Document doc = null;
            try {
                doc = Jsoup.connect(url).userAgent("Mozilla").get();//模拟火狐浏览器
            } catch (IOException e) {
                e.printStackTrace();
            }
            ExecutorService workersPool =  Executors.newFixedThreadPool(5 * Runtime.getRuntime().availableProcessors() );
            //这里根据在网页中分析的类选择器来获取电影列表所在的节点
            Elements div = doc.getElementsByClass("rr");
            //获取所有详情的链接所在的节点
//            Elements tr = doc.getElementsByClass("rcx12 rcm9 rcl9");
            for (int i = 1; i < div.size(); i++) {
                if(i>1){
                    break;
                }
                Element element = div.get(i);
                //缩略图url
                String imgSrc = element.select("div").get(1).getElementsByTag("amp-img").attr("src");
                System.out.println("imgSrc = " + imgSrc);
                String title = element.select("div").get(1).getElementsByTag("amp-img").attr("alt");

                System.out.println(title);
                File file = new File("E:\\test2\\" + title + ".jpg");
                long startTime = System.currentTimeMillis();
                Future<Integer> future = workersPool.submit(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        try {
                            X509TrustUtiil.downloadFile(imgSrc, file, null);
                            System.out.println("startTime = " + startTime);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return 0;
                    }
                });

                // TODO 结束所有线程
                workersPool.shutdown();

                // TODO 这个方法是统计所有线程全部执行完毕的时间
                while(true){
                    if(workersPool.isTerminated()){
                        long end = System.currentTimeMillis();
                        System.out.println("线程执行用时: " + (end - startTime) + "ms");
                        break;
                    }
                }
            }
        }
    }



    //            File f = new File("E:\\test2\\" + title + ".jpg");
//
//            org.apache.commons.io.FileUtils.copyURLToFile(httpurl, f);


//            String videos = tr.get(1).select("src").attr("abs:href");
//            System.out.println(tr.get(1).select("alt").attr("abs:href") + "\t" + tr.get(1).select("a").text());
//            //这里要跳过这个首页页面 否则会抛出异常0 = {Element@6494} "<div class="rcm4"> \n <a href="/post/18428/"> \n  <amp-img width="250" height="250" data-toggle="tooltip" title="点击查看高清大图" src="https://s3.qingbuyaohaixiu.com/CACHE/images/image/097703092d5ff7b70cf01663626f81a1/b9db4a517db7e9fc21b7ef1cc978fd45.jpeg" alt="背影" /> </a> \n <br> \n <a href="/post/18428/"> 背影<ins><sub>点击查看高清大图</sub></ins> </a> \n</div>"
////            if ("http://www.dytt8.net/html/gndy/jddy/index.html".equals(videos)) continue;
//            //进如电影列表详情页面
//            doc = Jsoup.connect(videos).userAgent("Mozilla").get();
//            //获取到电影详情页面所在的节点
//            Element div1 = doc.getElementById("Zoom");
//            //获取电影描述
//            //String des=div1.select("p").text();
//            //System.out.println(div1.select("p").text());
//            //获取封面图地址
//            Elements select = div1.select("img[src$=.jpg]");
//            String imgUrl = select.get(0).attr("abs:src");
//            System.out.println(imgUrl);
//            //获取下载地址
//            System.out.println(div1.select("td").text());

//            //存入数据库
//            try {
//                Db.use().insert(
//                        Entity.create("movie")
//                                .set("type", "最新电影")
//                                .set("title", tr.get(1).select("a").text())
//                                .set("imagesUrl", imgUrl)
//                                .set("videoUrl", div1.select("td").text())
//                                .set("date", new Date())
//                                .set("describe", div1.select("p").text())
//
//                );
//            } catch (SQLException e) {
//                e.printStackTrace();

}

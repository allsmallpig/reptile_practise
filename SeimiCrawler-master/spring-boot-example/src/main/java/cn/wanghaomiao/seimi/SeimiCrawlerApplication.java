package cn.wanghaomiao.seimi;

import cn.wanghaomiao.seimi.util.X509TrustUtiil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
@EnableScheduling
public class SeimiCrawlerApplication {
    static ThreadPoolExecutor workersPool;
    static Logger logg = LoggerFactory.getLogger(SeimiCrawlerApplication.class);

    static {
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        workersPool = new ThreadPoolExecutor(10, 20,
                10000L, TimeUnit.MILLISECONDS,
                workQueue, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    //	static ExecutorService workersPool =  Executors.newFixedThreadPool(5 * Runtime.getRuntime().availableProcessors() );
    static Lock lock = new ReentrantLock();
    static Lock lock4For = new ReentrantLock();
    static Lock lock4ForFinal = new ReentrantLock();


    public static void main(String[] args) throws Exception {
        SpringApplication.run(SeimiCrawlerApplication.class, args);
        pachong3();
    }

    //支持分页爬取
    public static void pachong3() throws Exception {
        //设置需要下载多少页
        //先爬取10页的内容

        ArrayList<String> utlList = new ArrayList<>();
        for (int i = 46; i <= 47; i++) {
            String url = "https://qingbuyaohaixiu.com/?page=" + i;
            utlList.add(url);
        }
        try {
            pachong_page(utlList);
            //让线程操作不要太快 1秒一次 时间自己设置，主要是模拟人在点击
        } catch (Exception e) {
            logg.error(String.valueOf(e.getStackTrace()));
            e.printStackTrace();
        }

        System.out.println("爬取结束！一共爬取内容为:" );
    }

    public static void pachong_page(ArrayList<String> utlList) throws Exception {
        ArrayList<Elements> elementsList = new ArrayList<>();

        for (String url : utlList) {
            Document doc = null;
            try {
                //模拟火狐浏览器
                doc = Jsoup.connect(url).timeout(50000).userAgent("Mozilla").get();
                Thread.sleep(3000);
            } catch (IOException e) {
                logg.error(String.valueOf(e.getCause()));
                e.printStackTrace();
                continue;
            }
            if(null!=doc){
                Elements tr = doc.getElementsByClass("rcm4");
                if(null!=tr){
                    elementsList.add(tr);
                }
            }
        }

        List<String> urls = new ArrayList<String>();
        //获取所有详情的链接所在的节点
        elementsList.stream().forEach(tr -> {
            tr.stream().forEach(element -> {
                //高清图url
                String aurl = element.select("a").attr("abs:href");
                urls.add(aurl);
                System.out.println("网址集合的尺寸 = " + urls.size());
            });
        });

        //获取列表总数
//		int result = tr.size();
//
//		for (int i = 1; i < tr.size(); i++) {
//			Element element = tr.get(i - 1);
//            try {
//                Thread.sleep(1000);//让线程操作不要太快 1秒一次 时间自己设置，主要是模拟人在点击
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

        //高清图url
//			String aurl = element.select("a").attr("abs:href");
//			urls.add(aurl);
//			System.out.println("网址集合的尺寸 = " + urls.size());
        //缩略图url
//            String imgSrc = element.getElementsByTag("amp-img").attr("src");
//            System.out.println("imgSrc = " + imgSrc);
//            String title = element.getElementsByTag("amp-img").attr("alt");
//            System.out.println(title);
//            File file = new File("E:\\test2\\" + title + ".jpg");
//            X509TrustUtiil.downloadFile(imgSrc, file, null);
//		}
        getMoore(urls);
    }


    public static void getMoore(List<String> urls) throws Exception {
        Iterator<String> iterator = urls.iterator();
        boolean sync = true;
        while (iterator.hasNext() && sync) {
            sync = false;
            String url = iterator.next();
            Document doc = null;
            try {
                //模拟火狐浏览器
                doc = Jsoup.connect(url).timeout(50000).userAgent("Mozilla").get();
                Thread.sleep(3000);
            } catch (IOException e) {
                logg.error(String.valueOf(e.getCause()));
                e.printStackTrace();
                continue;
            }
            if (null != doc) {
                long startTime = System.currentTimeMillis();
                System.out.println("startTime = " + startTime);
                //这里根据在网页中分析的类选择器来获取电影列表所在的节点
                Elements div = doc.getElementsByClass("rr");
                if(null!=div){
                    div.stream().filter(element -> !StringUtils.isBlank(element.select("div").get(1).getElementsByTag("amp-img").attr("src")))
                            .forEach(element -> {
                                String imgSrc = element.select("div").get(1).getElementsByTag("amp-img").attr("src");
                                System.out.println("imgSrc = " + imgSrc);
                                String title = element.select("div").get(1).getElementsByTag("amp-img").attr("alt");

                                System.out.println(title);
                                File file = new File("E:\\test2\\" + title + ".jpg");
                                workersPool.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            X509TrustUtiil.downloadFile(imgSrc, file, null);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            });
                    sync = true;
                }
            }
        }
    }
}

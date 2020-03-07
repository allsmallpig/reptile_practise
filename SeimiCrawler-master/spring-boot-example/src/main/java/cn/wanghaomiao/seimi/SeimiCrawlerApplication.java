package cn.wanghaomiao.seimi;

import cn.wanghaomiao.seimi.util.X509TrustUtiil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.common.util.IoUtils;
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
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
public class SeimiCrawlerApplication {
    static ThreadPoolExecutor workersPool;
    static Logger logg = LoggerFactory.getLogger(SeimiCrawlerApplication.class);
    public static final String FIRSTURL = "E:\\firstPicUrl.txt";
    public static final String SECONDURL = "E:\\secondPicUrl.txt";
    public static final String NUMFILE = "E:\\numUrl.txt";
    public static final String BASEURL = "https://qingbuyaohaixiu.com/?page=";

    static {
        LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        workersPool = new ThreadPoolExecutor(10, 20,
                10000L, TimeUnit.MILLISECONDS,
                workQueue, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    //	static ExecutorService workersPool =  Executors.newFixedThreadPool(5 * Runtime.getRuntime().availableProcessors() );

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SeimiCrawlerApplication.class, args);
//        pachong3();
    }

    @Component
    public class ScheduledTasks {

        //输出时间格式
        private final SimpleDateFormat format = new SimpleDateFormat("HH(hh):mm:ss S");

        /* 第3步 五分钟*/
        @Scheduled(initialDelay = 60000, fixedRate = 60000)
        public void threeScheduledTasks() throws Exception {
            List<String> secondUrl2Three = IOUtils.readLines(new FileInputStream(new File(SECONDURL)));
            if (!CollectionUtils.isEmpty(secondUrl2Three)) {
                logg.info("第 三 步开始执行，现在时间是 : " + format.format(new Date()));
                getMoore(secondUrl2Three);
            }
        }

        /* 第二步 一分钟*/
        @Scheduled(initialDelay = 180000, fixedRate = 180000)
        public void secondScheduledTasks() throws Exception {
            List<String> firstUrl2Second = IOUtils.readLines(new FileInputStream(new File(FIRSTURL)));
            if (!CollectionUtils.isEmpty(firstUrl2Second)) {
                logg.info("第 二 步开始执行，现在时间是 : " + format.format(new Date()));
                pachong_page(firstUrl2Second);
            }
        }

        /* 第一步 十分钟*/
        @Scheduled(initialDelay = 320000, fixedRate = 320000)
        public void firstScheduledTasks() {
            try {
                pachongFrist();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //支持分页爬取
    public static void pachongFrist() throws Exception {
        File file = new File(NUMFILE);
        List<String> numList = IOUtils.readLines(new FileInputStream(file));
        Integer firstNum = 0;
        if (!CollectionUtils.isEmpty(numList)) {
            numList = numList.parallelStream().sorted(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return Integer.parseInt(o1) - Integer.parseInt(o2);
                }
            }).collect(Collectors.toList());
            firstNum = Integer.valueOf(numList.get(0));
            numList.remove(0);
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(file);
                FileUtils.deleteQuietly(file);
                file = new File(NUMFILE);
                IOUtils.writeLines(numList, null, fout);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ArrayList<String> utlList = new ArrayList<>();
        for (int i = firstNum; i <= firstNum; i++) {
            String url = BASEURL + i;
            logg.info("第一步添加到第二步执行的url " + url);
            utlList.add(url);
        }

        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(new File(FIRSTURL));
            IOUtils.writeLines(utlList, null, fout);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logg.info("第一步执行完毕  " + JSON.toJSONString(utlList));
    }

    public static void pachong_page(List<String> utlList) throws Exception {
        if (!CollectionUtils.isEmpty(utlList)) {
            FileOutputStream fout = null;
            try {
                File file = new File(FIRSTURL);
                fout = new FileOutputStream(file);
                FileUtils.deleteQuietly(file);
                file = new File(FIRSTURL);
                IOUtils.writeLines(new ArrayList<>(0), null, fout);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<Elements> elementsList = new ArrayList<>();
        Iterator<String> iterator = utlList.iterator();
        while (iterator.hasNext()) {
            String url = iterator.next();
            logg.info("第二步入参网址 ={}", url);
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
                Elements tr = doc.getElementsByClass("rcm4");
                if (null != tr) {
                    elementsList.add(tr);
                }
            }
            //删除第一步
            iterator.remove();
        }

        List<String> urls = new ArrayList<String>();
        //获取所有详情的链接所在的节点
        elementsList.stream().forEach(tr -> {
            tr.stream().forEach(element -> {
                //高清图url
                String aurl = element.select("a").attr("abs:href");
                urls.add(aurl);
                logg.info("第二步子节点 网址集合的尺寸 ={},当前子节点网址是 ", urls.size(), aurl);
            });
        });

        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(new File(SECONDURL));
            IOUtils.writeLines(urls, null, fout);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logg.info("第二步执行完毕 = " + urls.size());
    }


    public static void getMoore(List<String> urls) throws Exception {
        if (!CollectionUtils.isEmpty(urls)) {
            FileOutputStream fout = null;
            try {
                File file = new File(SECONDURL);
                fout = new FileOutputStream(file);
                FileUtils.deleteQuietly(file);
                file = new File(SECONDURL);
                IOUtils.writeLines(new ArrayList<>(0), null, fout);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Iterator<String> iterator = urls.iterator();
        boolean sync = true;
        while (iterator.hasNext() && sync) {
            sync = false;
            String url = iterator.next();
            logg.info("第三步入参网址 ={}", url);
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
                if (null != div) {
                    div.stream().filter(element -> !StringUtils.isBlank(element.select("div").get(1).getElementsByTag("amp-img").attr("src")))
                            .forEach(element -> {
                                String imgSrc = element.select("div").get(1).getElementsByTag("amp-img").attr("src");
                                logg.info("第三步子节点执行网址:{}", imgSrc);
                                String title = element.select("div").get(1).getElementsByTag("amp-img").attr("alt");
                                logg.info("第三步子节点执行标题:{}", title);
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
            iterator.remove();
        }
        logg.info("第三步执行完毕  ");
    }
}

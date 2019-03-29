package com;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class task {

    private static final String URL = "http://e0321.p16.rocks/v.php?next=watch&page=";

    public static void main (String[] args) {
        Map<String,String> header = new HashMap<>();
        header.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0");
        header.put("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        header.put("Accept-Language","zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        header.put("Referer","http://e0302.p29.rocks/video.php?category=rf&page=1");
        header.put("Alt-Used","e0302.p29.rocks:443");
        header.put("Connection","keep-alive");
        header.put("Upgrade-Insecure-Requests","1");
        header.put("Pragma","no-cache");
        header.put("Cache-Control","no-cache");

        /*获取更新*/
        new Thread(() -> {
            while (true){
                try {
                    analysis(getRes("http://e0321.p16.rocks/v.php?next=watch&page=1", header));
                    Thread.sleep(30000);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        int paga = 0;
        while (true) {
            String body = "";
            paga++;
            try {
                body = getRes(URL + paga, header);
            } catch (Exception ignored) {
                ignored.printStackTrace();
                System.out.println("读取失败");
            }
            analysis(body);
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void analysis(String body){
        if(!"".equals(body) && null != body){
            Elements eles = Jsoup.parse(body).select("div.listchannel");
            for (Element ele : eles) {
                Element temp = ele.select("a").first();
                String title = temp.select("img").attr("title");
                String src = temp.select("img").attr("src");
                String details = temp.attr("href");
                addTake(title,details,src);
            }
        }
    }

    private static void addTake(String filenam, String url,String img){
        JDBCUtils.insert(filenam,url,img);
    }

    private static void download(String url,String filename) {
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
                .readTimeout(300,TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            ReadableByteChannel readableByteChannel = Channels.newChannel(response.body().byteStream());
            FileOutputStream fileOutputStream = new FileOutputStream("D:/v9/"+filename+".mp4");
            FileChannel fs = fileOutputStream.getChannel();
            fs.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            fs.force(true);
            readableByteChannel.close();
            fileOutputStream.close();
            fs.close();
        } catch (IOException ignored) {
        }
    }

    private static String getRes(String url, Map<String, String> header) throws IOException {
        return Jsoup.connect(url)
                .headers(header)
                .execute()
                .body();
    }


}

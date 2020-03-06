package cn.wanghaomiao.seimi.util;

/**
 * @author
 * @packageName cn.wanghaomiao.seimi.util
 * @date 2020/2/27 23:27
 * @Description <a href="goodmanalibaba@foxmail.com"></a>
 * @Versin 1.0
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

public class X509TrustUtiil implements X509TrustManager {

    /**
     *      * TODO 下载文件到本地
     *      *
     *      * @param fileUrl   远程地址
     *      * @param fileLocal 本地路径
     *      * @throws Exception
     *     
     */
    public static void downloadFile(String fileUrl, File fileLocal, String callId) throws Exception {
        SSLContext sslcontext = SSLContext.getInstance("SSL", "SunJSSE");
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,SSLv3");
        sslcontext.init(null, new TrustManager[]{new X509TrustUtiil()}, new java.security.SecureRandom());
        URL url = new URL(fileUrl);
        HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslsession) {
                System.out.println("WARNING: Hostname is not matched for cert.");
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
        HttpsURLConnection urlCon = (HttpsURLConnection) url.openConnection();
        urlCon.setConnectTimeout(70000);
        urlCon.setReadTimeout(70000);
        urlCon.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
        int code = urlCon.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) {
            throw new Exception("文件读取失败");
        }
        // 读文件流
        DataInputStream in = new DataInputStream(urlCon.getInputStream());
        DataOutputStream out = new DataOutputStream(new FileOutputStream(fileLocal));
        byte[] buffer = new byte[2048];
        int count = 0;
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        out.close();
        in.close();
    }


    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
    }


    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

}


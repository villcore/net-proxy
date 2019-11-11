package com.villcore.net.proxy.demo;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class DownloadVideosV2 {
    private static final Logger log = LoggerFactory.getLogger(DownloadVideosV2.class);

    private static final byte[] BUFFER_BYTES = new byte[4 * 1024];

    public static void main(String[] args) throws InterruptedException {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        String host = args[0];
        int descrDay = 0;
        for (int i = Integer.valueOf(args[1]); i < 4000; i++) {
            String requestPath = host + "/video/" + i + ".mp4";
            String url = requestPath;
            log.info("=================================");
            log.info("\t start download video {} ", url);

            Request request = new Request.Builder()
                    .get()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.132 Safari/537.36")
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                if (response.code() > 400) {
                    log.info("\t Err {} not found ", url);
                    break;
                }

                doSaveVideo(response, requestPath);
            } catch (Exception e) {
                log.info("\t Err {} io exception ", url, e.getMessage());
            }
        }
    }

    private static void doSaveVideo(Response response, String requestPath) {
        if (response == null) {
            return;
        }

        ResponseBody responseBody;
        if ((responseBody = response.body()) == null) {
            return;
        }

        Path filePath = Paths.get("i://videos", requestPath);
        try {
            if (Files.exists(filePath)) {
                return;
            }

            Path tmp = Paths.get("videos", requestPath + ".tmp");
            if (Files.exists(tmp)) {
                Files.delete(tmp);
            }

            Files.createDirectories(tmp.getParent());
            Files.createFile(tmp);

            int readTotal = 0;
            try (InputStream is = responseBody.byteStream(); OutputStream os = Files.newOutputStream(tmp)) {
                int read = 0;
                while ((read = is.read(BUFFER_BYTES)) >= 0) {
                    readTotal += read;
                    os.write(BUFFER_BYTES, 0, read);
                }
            }

            log.info("\t Success {} save to path {}, total read {} ", requestPath, tmp, readTotal);
            Files.move(tmp, filePath);

        } catch (Exception e) {
            e.printStackTrace();
            log.info("\t Err {} save to path {} ", filePath, e.getMessage());
        }
    }
}

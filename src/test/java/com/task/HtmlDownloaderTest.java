package com.task;

import com.task.HtmlDownloader;
import io.undertow.Undertow;
import io.undertow.util.Headers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;

import static io.undertow.Handlers.path;

class HtmlDownloaderTest {
    Undertow server;
    String testDirPath;

    @BeforeEach
    public void startServer() {
        server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(path()
                        .addPrefixPath("/", exchange -> {
                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
                            exchange.getResponseSender().send("<html>" +
                                    "<body>" +
                                    "<a href=\"/link\">link</a>" +
                                    "</body>" +
                                    "</html>");
                        })
                        .addPrefixPath("/link", exchange -> {
                            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
                            exchange.getResponseSender().send("<html>" +
                                    "<body>" +
                                    "</body>" +
                                    "</html>");
                        }))
                .build();
        server.start();
        String currentDir = System.getProperty("user.dir");
        testDirPath = currentDir + File.separator + "test";
    }

    @AfterEach
    public void stopServer() {
        server.stop();
        File testDir = new File(testDirPath);
        testDir.delete();
    }

    @Test
    void download() {
        HtmlDownloader.download("http://localhost:8080", testDirPath);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {
        }
        File testDir = new File(testDirPath);
        File[] files = testDir.listFiles();
        Assertions.assertNotNull(files);
        Assertions.assertEquals(2, files.length);
        Assertions.assertNotNull(Arrays.stream(files).filter(f -> "index.html".equals(f.getName())).findFirst().orElse(null));
        Assertions.assertNotNull(Arrays.stream(files).filter(f -> "link.html".equals(f.getName())).findFirst().orElse(null));
    }

}
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlDownloader {

    private static final Pattern LINK_PATTERN = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1");
    private static final Executor EXECUTOR = Executors.newScheduledThreadPool(2);

    public void download(String url, String downloadsPath) {
        System.out.println("Start downloading");
        try {
            Saver.createDirIfNotExists(downloadsPath);
            doDownload(url, "", downloadsPath, "index.html", ConcurrentHashMap.newKeySet(), true);
        } catch (RuntimeException e) {
            printError(url, e);
        }
    }

    private void doDownload(String host, String subLink, String downloadsPath, String fileName, Set<String> knownLinks, boolean fetchChildAsync) {
        if (host == null) {
            return;
        }
        String html;
        try {
            html = downloadHtml(host, subLink);
        } catch (RuntimeException e) {
            printError(subLink, e);
            return;
        }
        Saver.createSubDirs(downloadsPath, fileName);
        Saver.saveOnDisk(html, downloadsPath + fileName);

        Set<String> links = getLinks(html, knownLinks);
        ProgressBar progressBar = null;
        if (fetchChildAsync) {
            progressBar = new ProgressBar(links.size());
        }
        ProgressBar finalProgressBar = progressBar;

        knownLinks.addAll(links);
        links.forEach(link -> {
            String linkFileName = link + ".html";
            if (fetchChildAsync) {
                CompletableFuture.runAsync(() -> doDownload(host, link, downloadsPath, linkFileName, knownLinks, false), EXECUTOR)
                        .thenRun(finalProgressBar::up);
            } else {
                doDownload(host, link, downloadsPath, linkFileName, knownLinks, false);
            }
        });
    }

    private String downloadHtml(String host, String subLink) {
        StringBuilder result = new StringBuilder();
        try {
            String encodedUrl = String.format("%s%s", host, encode(subLink));
            URL u = new URL(encodedUrl);
            try (InputStream is = u.openStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private Set<String> getLinks(String html, Set<String> knownLinks) {
        Matcher matcher = LINK_PATTERN.matcher(html);
        Set<String> links = new HashSet<>();
        while (matcher.find()) {
            String link = matcher.group(2);
            if (isLinkValid(link, knownLinks)) {
                links.add(adjustLink(link));
            }
        }
        return links;
    }

    private String adjustLink(String link) {
        if (!link.startsWith("/")) {
            return "/" + link;
        }
        return link;
    }

    private boolean isLinkValid(String link, Set<String> knownLinks) {
        return !link.startsWith("http")
                && !link.contains("#")
                && !link.contains("@")
                && !link.contains("mailto")
                && !link.contains("tel:+")
                && !link.contains("{filename}")
                && !"/".equals(link) && !knownLinks.contains(link);
    }

    private void printError(String url, RuntimeException e) {
        System.out.printf("An error occurred while downloading %s: %s\n", url, e.getMessage());
    }

    private String encode(String link) {
        if (link.contains("/")) {
            String[] split = link.split("/");
            StringBuilder sb = new StringBuilder("/");
            for (int i = 0; i < split.length; i++) {
                sb.append(URLEncoder.encode(split[i], StandardCharsets.UTF_8));
                if (i + 1 != split.length)
                    sb.append("/");
            }
            return sb.toString();
        }
        return URLEncoder.encode(link, StandardCharsets.UTF_8);
    }
}

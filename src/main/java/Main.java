import java.io.File;

public class Main {
    public static void main(String[] args) {
        String currentDir = System.getProperty("user.dir");
        HtmlDownloader htmlDownloader = new HtmlDownloader();
        htmlDownloader.download("https://tretton37.com", currentDir + File.separator + "tretton37" + File.separator);
    }
}

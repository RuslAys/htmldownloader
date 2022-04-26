package com.task;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        HtmlDownloader.download("https://tretton37.com", getPathToSave(args));
    }

    private static String getPathToSave(String[] args) {
        if (args.length != 1) {
            String currentDir = System.getProperty("user.dir");
            return currentDir + File.separator + "tretton37" + File.separator;
        }
        return args[0];
    }
}

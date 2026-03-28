package com.kevin.aitechnotes.util;

public class UrlNormalizer {

    public static String normalize(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }

        String result = url.strip();
        result = result.toLowerCase();

        if(result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }
}

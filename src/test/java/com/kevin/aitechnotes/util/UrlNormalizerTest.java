package com.kevin.aitechnotes.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class UrlNormalizerTest {

    @Test
    void 應該移除前後空白() {
        String input = "  https://example.com  ";
        String expected = "https://example.com";
        assertEquals(expected, UrlNormalizer.normalize(input));
    }

    @Test
    void 應該轉為小寫() {
        String input = "HTTPS://EXAMPLE.COM/Article";
        String expected = "https://example.com/article";
        assertEquals(expected, UrlNormalizer.normalize(input));
    }

    @Test
    void 應該移除結尾斜線() {
        String input = "https://example.com/";
        String expected = "https://example.com";
        assertEquals(expected, UrlNormalizer.normalize(input));
    }

    @Test
    void 三個規則一起套用() {
        String input = "  HTTPS://EXAMPLE.COM/Path/  ";
        String expected = "https://example.com/path";
        assertEquals(expected, UrlNormalizer.normalize(input));
    }

    @Test
    void null輸入應該回傳空字串() {
        assertEquals("", UrlNormalizer.normalize(null));
    }

    @Test
    void 空字串應該回傳空字串() {
        assertEquals("", UrlNormalizer.normalize(""));
    }
}

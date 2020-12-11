package com.shadorc.shadbot.utils;

import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class NetUtilsTest {

    @Test
    public void testIsUrl() {
        assertTrue(NetUtils.isUrl("http://www.youtube.com"));
        assertTrue(NetUtils.isUrl("https://www.youtube.com"));
        assertTrue(NetUtils.isUrl("https://www.you%20tube.com"));
        assertTrue(NetUtils.isUrl("https://www.(you)%20tube.com"));
        assertFalse(NetUtils.isUrl("https://www.you tube.com"));
        assertFalse(NetUtils.isUrl("youtube"));
        assertFalse(NetUtils.isUrl("youtube.com"));
        assertFalse(NetUtils.isUrl("www.youtube.com"));
    }

    @Test
    public void testCleanWithLinebreaks() {
        assertEquals("Test", NetUtils.cleanWithLinebreaks("<html>Test</html>"));
        assertEquals("", NetUtils.cleanWithLinebreaks("<html></html>"));
        assertEquals("", NetUtils.cleanWithLinebreaks(""));
        assertEquals("Hello\\nWorld", NetUtils.cleanWithLinebreaks("Hello<br>World"));
        assertEquals("\\n\\nHello\\nWorld", NetUtils.cleanWithLinebreaks("<p>Hello</p><br>World"));
        assertEquals("\\n\\n", NetUtils.cleanWithLinebreaks("<br><br>"));
        assertEquals("\\n\\n", NetUtils.cleanWithLinebreaks("<p></p>"));
        assertEquals("\\n\\n", NetUtils.cleanWithLinebreaks("<p>"));
        assertEquals("&lt;1&gt;", NetUtils.cleanWithLinebreaks("<1>"));
        assertNull(NetUtils.cleanWithLinebreaks(null));
    }

    @Test
    public void testEncode() {
        assertNull(NetUtils.encode(null));
        assertEquals("", NetUtils.encode(""));
        final String input = "&é'(-è_çà)=az %µ¨£^$ù*,;:!?./§ertyuiop^1234567890é\\#\"";
        assertEquals(URLEncoder.encode(input, StandardCharsets.UTF_8), NetUtils.encode(input));
    }

}

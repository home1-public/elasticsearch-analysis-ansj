package org.ansj.test.org.ansj.elasticsearch.pubsub.redis;

import org.junit.Test;

import java.util.regex.Pattern;

/**
 * Created by zhl on 15/7/14.
 */
public class FileUtilsTest {

    @Test
    public void testMatch() {
        final Pattern p = Pattern.compile("^满意\\D*$");
        System.out.println(p.matcher("满意  满      a       意      a").matches());
        System.out.println(p.matcher("满哈-满,意").matches());
        System.out.println("满哈-满,意".replace(",", "\t"));
    }
}

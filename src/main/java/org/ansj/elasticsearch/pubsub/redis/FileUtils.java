package org.ansj.elasticsearch.pubsub.redis;

import org.ansj.elasticsearch.index.config.AnsjElasticConfigurator;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static ESLogger log = Loggers.getLogger("ansj-redis-msg-file");

    public static void remove(String content) {
        try {
            final File file = new File(AnsjElasticConfigurator.environment.configFile(), "ansj/user/ext.dic");
            removeFile(content, file, false);
        } catch (final IOException e) {
            log.warn("file not found $ES_HOME/config/ansj/user/ext.dic or read exception", e);
        }
    }

    public static void append(final String content) {
        try {
            final File file = new File(AnsjElasticConfigurator.environment.configFile(), "ansj/user/ext.dic");
            appendFile(content, file);
        } catch (final IOException e) {
            log.warn("read exception", e);
        }
    }

    public static void removeAMB(final String content) {
        try {
            final File file = new File(AnsjElasticConfigurator.environment.configFile(), "ansj/ambiguity.dic");
            removeFile(content, file, true);
        } catch (final IOException e) {
            log.warn("file not found $ES_HOME/config/ansj/user/ext.dic or read exception", e);
        }
    }

    public static void appendAMB(final String content) {
        try {
            final File file = new File(AnsjElasticConfigurator.environment.configFile(), "ansj/ambiguity.dic");
            appendFile(content, file);
        } catch (final IOException e) {
            log.warn("read exception", e);
        }
    }

    private static void appendFile(final String content, final File file) throws IOException {
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(content);
            writer.newLine();
        }
    }

    private static void removeFile(final String content, final File file, final boolean head) throws IOException {
        try (
                final BufferedReader reader = new BufferedReader(new FileReader(file));
                final BufferedWriter writer = new BufferedWriter(new FileWriter(file))
        ) {
            final List<String> list = new ArrayList<>();

            String text;
            while ((text = reader.readLine()) != null) {
                final boolean match = match(content, text, head);
                if(log.isDebugEnabled()) {
                    log.debug("match is {} text is{}", match, text);
                }
                if (match) {
                    list.add(text);
                }
            }

            for (final String item : list) {
                writer.write(item);
                writer.newLine();
            }
        }
    }

    private static boolean match(final String content, final String text, final boolean head) {
        return head ? !text.trim().matches("^" + content + "\\D*$") : !text.trim().equals(content);
    }
}

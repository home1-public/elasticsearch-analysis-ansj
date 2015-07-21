package org.ansj.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Synchronized;
import lombok.ToString;
import org.ansj.lucene.util.AnsjTokenizer;
import org.ansj.lucene5.AnsjIndexAnalysis;
import org.ansj.lucene5.AnsjQueryAnalysis;
import org.ansj.splitWord.IndexAnalysis;
import org.ansj.splitWord.ToAnalysis;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.Set;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;
import static org.ansj.AnsjContext.CONTEXT;
import static org.ansj.AnsjContext.refreshContext;
import static org.ansj.AnsjUtils.filesystemResource;
import static org.ansj.AnsjUtils.linesWithoutBlankAndComments;

@Builder(builderMethodName = "contextBuilder")
@ToString
@AllArgsConstructor(access = PRIVATE)
public class AnsjAnalysisPluginContext {

    //
    private static volatile AnsjAnalysisPluginContext _INSTANCE;

    public static AnsjAnalysisPluginContext PLUGIN_CONTEXT(final Settings indexSettings, final Settings settings) {
        return _INSTANCE != null ? _INSTANCE : newContextIfNoOneExists(indexSettings, settings);
    }

    public static AnsjAnalysisPluginContext PLUGIN_CONTEXT() {
        if (_INSTANCE != null) {
            return _INSTANCE;
        }
        throw new IllegalStateException("context not initialized");
    }

    @Synchronized
    private static AnsjAnalysisPluginContext newContextIfNoOneExists(final Settings indexSettings, final Settings settings) {
        if (_INSTANCE != null) {
            return _INSTANCE;
        } else {
            _INSTANCE = new AnsjAnalysisPluginContext(new Environment(indexSettings), settings);
            return _INSTANCE;
        }
    }

    public static ESLogger logger = Loggers.getLogger("ansj-analyzer");

    public final boolean nameRecognition;
    public final boolean numRecognition;
    public final boolean quantifierRecognition;
    public final String userLibraryLocation;
    public final String userAmbiguityLibraryLocation;
    public final boolean pstemming;//是否提取词干
    public final Set<String> stopFilter;

    public AnsjAnalysisPluginContext(final Environment environment, final Settings settings) {
        this(
                settings.getAsBoolean("nameRecognition", true),
                settings.getAsBoolean("numRecognition", true),
                settings.getAsBoolean("quantifierRecognition", true),
                absolutePath(environment, settings.get("userLibraryLocation", "ansj/user")),
                absolutePath(environment, settings.get("userAmbiguityLibraryLocation", "ansj/ambiguity.dic")),
                settings.getAsBoolean("pstemming", false),
                stopFilter(environment, settings)
        );
        this.refresh();
        logger.debug("用户词典路径:{}", CONTEXT().userLibraryLocation);
        logger.debug("歧义词典路径:{}", CONTEXT().userAmbiguityLibraryLocation);

        ToAnalysis.parse("一个词");
        logger.info("ansj分词器预热完毕，可以使用!");
    }

    public AnsjIndexAnalysis indexAnalyzer() {
        return new AnsjIndexAnalysis(this.stopFilter, this.pstemming);
    }

    public AnsjQueryAnalysis queryAnalyzer() {
        return new AnsjQueryAnalysis(this.stopFilter, this.pstemming);
    }

    public Tokenizer indexTokenizer(final Reader reader) {
        return new AnsjTokenizer(new IndexAnalysis(new BufferedReader(reader), null), this.stopFilter, this.pstemming);
    }

    public Tokenizer queryTokenizer(final Reader reader) {
        return new AnsjTokenizer(new ToAnalysis(new BufferedReader(reader), null), this.stopFilter, this.pstemming);
    }

    public Tokenizer tokenizer(final Reader reader) {
        return new AnsjTokenizer(new IndexAnalysis(new BufferedReader(reader), null), this.stopFilter, pstemming);
    }

    public void refresh() {
        refreshContext(
                CONTEXT()
                        .withNameRecognition(this.nameRecognition)
                        .withNumRecognition(this.numRecognition)
                        .withQuantifierRecognition(this.quantifierRecognition)
                        .withRealName(false)
                        .withSkipUserDefine(false)
                        .withUserLibraryLocation(this.userLibraryLocation)
                        .withUserAmbiguityLibraryLocation(this.userAmbiguityLibraryLocation)
        );
    }

    public static Set<String> stopFilter(final Environment environment, final Settings settings) {
        final Set<String> stopFilter;
        final boolean enabledStopFilter = settings.getAsBoolean("enabledStopFilter", false);
        if (enabledStopFilter) {
            final String stopLibraryPath = absolutePath(environment, settings.get("stopLibraryLocation", "ansj/stopLibrary.dic"));
            logger.debug("停止词典路径:{}", stopLibraryPath);
            stopFilter = linesWithoutBlankAndComments(filesystemResource(stopLibraryPath), l -> l).stream().collect(toSet());
            logger.info("停止词典加载完毕!");
        } else {
            stopFilter = newLinkedHashSet();
        }
        return stopFilter;
    }

    public static String absolutePath(final Environment environment, final String file) {
        // environment.pluginsFile()
        return new File(environment.configFile(), file).getAbsolutePath();
    }
}

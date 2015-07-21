package org.ansj.elasticsearch;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.settings.IndexSettings;

import java.io.Reader;

import static org.ansj.elasticsearch.AnsjAnalysisPluginContext.PLUGIN_CONTEXT;

/**
 * by zhangqinghua on 14-9-3.
 */
public class AnsjIndexTokenizerFactory extends AbstractTokenizerFactory {

    @Inject
    public AnsjIndexTokenizerFactory(
            final Index index, @IndexSettings final Settings indexSettings,
            @Assisted final String name, @Assisted final Settings settings
    ) {
        super(index, indexSettings, name, settings);
        PLUGIN_CONTEXT(indexSettings, settings);
    }

    @Override
    public Tokenizer create(final Reader reader) {
        return PLUGIN_CONTEXT().indexTokenizer(reader);
    }
}
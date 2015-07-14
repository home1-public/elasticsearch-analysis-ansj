package org.ansj.elasticsearch.index.tokenizer;

import org.ansj.lucene.util.AnsjTokenizer;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.settings.IndexSettings;

import java.io.BufferedReader;
import java.io.Reader;

import static org.ansj.elasticsearch.index.config.AnsjElasticConfigurator.*;

/**
 * by zhangqinghua on 14-9-3.
 */
public class AnsjQueryTokenizerFactory extends AbstractTokenizerFactory {

    @Inject
    public AnsjQueryTokenizerFactory(
            final Index index,
            @IndexSettings final Settings indexSettings,
            @Assisted final String name,
            @Assisted final Settings settings
    ) {
        super(index, indexSettings, name, settings);
        init(indexSettings, settings);
    }

    @Override
    public Tokenizer create(final Reader reader) {
        return new AnsjTokenizer(
                new ToAnalysis(new BufferedReader(reader)), filter, pstemming
        );
    }
}
package org.ansj.elasticsearch;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettings;

import static org.ansj.elasticsearch.AnsjAnalysisPluginContext.PLUGIN_CONTEXT;

public class AnsjIndexAnalyzerProvider extends AbstractIndexAnalyzerProvider<Analyzer> {

    private final Analyzer analyzer;

    @Inject
    public AnsjIndexAnalyzerProvider(
            final Index index, @IndexSettings final Settings indexSettings, final Environment environment,
            @Assisted final String name, @Assisted final Settings settings
    ) {
        super(index, indexSettings, name, settings);
        this.analyzer = PLUGIN_CONTEXT(indexSettings, settings).indexAnalyzer();
    }

    public AnsjIndexAnalyzerProvider(
            final Index index, final Settings indexSettings,
            final String name, final Settings settings
    ) {
        super(index, indexSettings, name, settings);
        this.analyzer = PLUGIN_CONTEXT(indexSettings, settings).indexAnalyzer();
    }

    public AnsjIndexAnalyzerProvider(
            final Index index, final Settings indexSettings, final String prefixSettings,
            final String name, final Settings settings
    ) {
        super(index, indexSettings, prefixSettings, name, settings);
        this.analyzer = PLUGIN_CONTEXT(indexSettings, settings).indexAnalyzer();
    }

    @Override
    public Analyzer get() {
        return this.analyzer;
    }
}

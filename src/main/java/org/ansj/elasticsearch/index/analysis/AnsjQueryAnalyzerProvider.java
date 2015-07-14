package org.ansj.elasticsearch.index.analysis;

import static org.ansj.elasticsearch.index.config.AnsjElasticConfigurator.filter;
import static org.ansj.elasticsearch.index.config.AnsjElasticConfigurator.init;
import static org.ansj.elasticsearch.index.config.AnsjElasticConfigurator.pstemming;

import org.ansj.lucene5.AnsjAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettings;

public class AnsjQueryAnalyzerProvider extends AbstractIndexAnalyzerProvider<Analyzer> {

    private final Analyzer analyzer;

    @Inject
    public AnsjQueryAnalyzerProvider(
            final Index index,
            @IndexSettings final Settings indexSettings,
            final Environment env,
            @Assisted final String name,
            @Assisted final Settings settings
    ) {
        super(index, indexSettings, name, settings);
        init(indexSettings, settings);
        this.analyzer = new AnsjAnalysis(filter, pstemming);
    }

    public AnsjQueryAnalyzerProvider(
            final Index index,
            final Settings indexSettings,
            final String name,
            final Settings settings
    ) {
        super(index, indexSettings, name, settings);
        init(indexSettings, settings);
        this.analyzer = new AnsjAnalysis(filter, pstemming);
    }

    public AnsjQueryAnalyzerProvider(
            final Index index,
            final Settings indexSettings,
            final String prefixSettings,
            final String name,
            final Settings settings
    ) {
        super(index, indexSettings, prefixSettings, name, settings);
        init(indexSettings, settings);
        this.analyzer = new AnsjAnalysis(filter, pstemming);
    }

    @Override
    public Analyzer get() {
        return this.analyzer;
    }
}

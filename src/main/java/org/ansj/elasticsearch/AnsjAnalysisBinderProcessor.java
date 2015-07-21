package org.ansj.elasticsearch;

import org.elasticsearch.index.analysis.AnalysisModule;

public class AnsjAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processTokenFilters(final TokenFiltersBindings tokenFiltersBindings) {

    }

    @Override
    public void processAnalyzers(final AnalyzersBindings analyzersBindings) {
        analyzersBindings.processAnalyzer("ansj_index", AnsjIndexAnalyzerProvider.class);
        analyzersBindings.processAnalyzer("ansj_query", AnsjQueryAnalyzerProvider.class);
        super.processAnalyzers(analyzersBindings);
    }

    @Override
    public void processTokenizers(final TokenizersBindings tokenizersBindings) {
        tokenizersBindings.processTokenizer("ansj_index_token", AnsjIndexTokenizerFactory.class);
        tokenizersBindings.processTokenizer("ansj_query_token", AnsjQueryTokenizerFactory.class);
        super.processTokenizers(tokenizersBindings);
    }
}

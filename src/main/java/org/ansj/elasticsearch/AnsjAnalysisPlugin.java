package org.ansj.elasticsearch;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;

public class AnsjAnalysisPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "analysis-ansj";
    }

    @Override
    public String description() {
        return "ansj analysis";
    }

    public void onModule(final AnalysisModule module) {
        module.addProcessor(new AnsjAnalysisBinderProcessor());
    }

    public void onModule(final RestModule module) {

    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        final Collection<Class<? extends Module>> modules = newArrayList();
        modules.add(AnsjRestModule.class);
        return modules;
    }
}
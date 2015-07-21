package org.ansj.elasticsearch;

import org.elasticsearch.common.inject.AbstractModule;

/**
 * by zhl on 15/7/21.
 */
public class AnsjRestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AnsjRestHandler.class).asEagerSingleton();
    }
}

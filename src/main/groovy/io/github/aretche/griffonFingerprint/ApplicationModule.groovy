package io.github.aretche.griffonFingerprint

import griffon.core.event.EventHandler
import griffon.core.injection.Module
import griffon.plugins.gsql.GsqlBootstrap
import org.codehaus.griffon.runtime.core.injection.AbstractModule
import org.kordamp.jipsy.ServiceProviderFor

@ServiceProviderFor(Module)
class ApplicationModule extends AbstractModule {
    @Override
    protected void doConfigure() {
        bind(GsqlBootstrap.class)
                .to(DatabaseBootstrap.class)
                .asSingleton()
        bind(EventHandler)
            .to(ApplicationEventHandler)
            .asSingleton()
    }
}
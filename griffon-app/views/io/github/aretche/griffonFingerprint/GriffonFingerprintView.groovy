package io.github.aretche.griffonFingerprint

import griffon.core.artifact.GriffonView
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import javax.annotation.Nonnull

@ArtifactProviderFor(GriffonView)
class GriffonFingerprintView {
    @MVCMember @Nonnull
    FactoryBuilderSupport builder
    @MVCMember @Nonnull
    GriffonFingerprintModel model

    void initUI() {
        builder.application(title: application.configuration['application.title'],
            sizeToScene: true, centerOnScreen: true, name: 'mainWindow') {
            scene(fill: WHITE, width: 640, height: 480) {
                flowPane(){
                    textArea(prefHeight: 360, prefWidth: 600,
                            text: bind(model.estadoProperty()))
                    hbox(prefWidth: 600){
                        button( prefWidth: 150,
                                id: 'detectarActionTarget', detectarAction,
                                text: 'Detectar')
                        button( prefWidth: 150,
                                id: 'capturarActionTarget', capturarAction,
                                text: 'Capturar')
                        button( prefWidth: 150,
                                id: 'enrolarActionTarget', enrolarAction,
                                text: 'Enrolar')
                        button( prefWidth: 150,
                                id: 'validarActionTarget', validarAction,
                                text: 'Validar')
                    }
                }
            }
        }
    }
}
package io.github.aretche.griffonFingerprint

import griffon.core.artifact.GriffonModel
import griffon.transform.FXObservable
import griffon.metadata.ArtifactProviderFor

@ArtifactProviderFor(GriffonModel)
class GriffonFingerprintModel {
    @FXObservable String clickCount = "0"
}
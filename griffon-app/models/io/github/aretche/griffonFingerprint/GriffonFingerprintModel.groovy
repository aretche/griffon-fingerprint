package io.github.aretche.griffonFingerprint

import com.digitalpersona.uareu.Fmd
import com.digitalpersona.uareu.Reader

import griffon.core.artifact.GriffonModel
import griffon.transform.FXObservable
import griffon.metadata.ArtifactProviderFor

@ArtifactProviderFor(GriffonModel)
class GriffonFingerprintModel {

    // Lector de huellas asociado (null si no se detectó ninguno)
    Reader reader
    // Última huella capturada (null en caso de error)
    Reader.CaptureResult captureResult
    // Última huella enrolada (null en caso de error)
    Fmd enrollmentFMD
    // Mesajes de estado de la aplicación
    @FXObservable
    String estado = ""
}
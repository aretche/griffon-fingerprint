package io.github.aretche.griffonFingerprint

import griffon.core.artifact.GriffonController
import griffon.core.controller.ControllerAction
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import griffon.transform.Threading
import groovy.util.logging.Slf4j

import javax.annotation.Nonnull

import com.digitalpersona.uareu.*

@Slf4j
@ArtifactProviderFor(GriffonController)
class GriffonFingerprintController implements Engine.EnrollmentCallback {
    @MVCMember
    @Nonnull
    GriffonFingerprintModel model

    /**
     * Lista los lectores de huellas y asocia al modelo el primero de la lista
     */
    @ControllerAction
    @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
    void detectar() {

        model.reader = null

        ReaderCollection readers = UareUGlobal.GetReaderCollection()

        //acquire available readers
        try {
            log.info "Detectando lectores de huellas digitales..."
            readers.GetReaders()
        } catch (UareUException e) {
            log.error "Excepción al invocar a ReaderCollection.GetReaders()", e
        }

        if (readers) {
            String lectores = "Lectores detectados: \n"
            Integer num = 1
            log.info "Enumerando los lectores detectados..."
            readers?.each { Reader reader ->
                Reader.Description rd = reader.GetDescription()
                lectores += "#${num} \n"
                lectores += "Fabricante/Modelo: ${rd.id.vendor_name} / ${rd.id.product_name} \n"
                lectores += "USB VID-PID: ${rd.id.vendor_id} / ${rd.id.product_id} \n"
                try {
                    reader.Open(Reader.Priority.COOPERATIVE)
                    Reader.Capabilities rc = reader.GetCapabilities()
                    reader.Close()
                    lectores += "Puede capturar? ${rc.can_capture ? 'Sí' : 'No'}.\n"
                    lectores += "Puede hacer streaming? ${rc.can_stream ? 'Sí' : 'No'}.\n"

                } catch (UareUException e) {
                    log.error "Excepción al invocar a GetCapabilities()", e
                }
                num++
            }
            lectores += "Asociando lector #1...\n"
            model.estado = lectores
            model.reader = readers.first()
        } else {
            log.warn "No se detectaron lectores de huellas digitales!"
            model.estado = "No se detectaron lectores conectados!"
        }
    }

    /**
     * Captura una huella y muestra su calidad
     */
    @ControllerAction
    @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
    void capturar() {

        model.estado = ""
        this.capturarHuella()

    }

    /**
     * Enrola una huella y la asocia al modelo para posteriores verificaicones
     */
    @ControllerAction
    @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
    void enrolar() {

        model.enrollmentFMD = null
        model.estado = ""

        if (!model.reader) {
            log.warn "No hay un lector seleccionado!"
            model.estado = "No hay un lector seleccionado!"
        } else {
            model.estado = "Enrolando el dedo indice derecho...\n"
            try {
                Engine engine = UareUGlobal.GetEngine()
                model.enrollmentFMD = engine.CreateEnrollmentFmd(Fmd.Format.ANSI_378_2004, this)
                if (!model.enrollmentFMD) {
                    model.estado = "No se pudo enrolar la huella!"
                } else {
                    model.estado = "Huella enrolada con éxito!"
                }
            } catch (UareUException e) {
                log.error "Excepción al enrolar huella:", e
            }


        }
    }

    /**
     * Valida la huella ingresada contra la enrolada
     */
    @ControllerAction
    @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
    void validar() {

        model.estado = ""
        if (!model.enrollmentFMD) {
            model.estado = "Primero debe enrolar una huella!"
        } else {
            // Capturo la huella para comparar
            this.capturarHuella()

            if (model.captureResult != null) {
                Engine engine = UareUGlobal.GetEngine()

                // Creo un arreglo para almacenar las dos FMDs a comparar
                Fmd[] m_fmds = new Fmd[2]
                m_fmds[0] = model.enrollmentFMD

                try {
                    // Creo el FMD de la huella recién capturada
                    Fmd fmd = engine.CreateFmd(model.captureResult.image, Fmd.Format.ANSI_378_2004)
                    if (fmd) {
                        m_fmds[1] = fmd

                        // Probabilidad de falso positivo de 1 en 100000
                        int target_falsematch_rate = Engine.PROBABILITY_ONE / 100000

                        int falsematch_rate = engine.Compare(m_fmds[0], 0, m_fmds[1], 0)

                        if (falsematch_rate < target_falsematch_rate) {
                            model.estado += "Las huellas coinciden.\n"
                            model.estado += String.format("Indice de disimilaridad: 0x%x.\n", falsematch_rate)
                            model.estado += String.format("Probabilidad de falso positivo: %e.\n\n\n", (double) (falsematch_rate / Engine.PROBABILITY_ONE))
                        } else {
                            model.estado += "Las huellas no coinciden!!!\n\n\n"
                        }
                    } else {
                        model.estado += "Error al generar el FMD de la huella!!!\n\n\n"
                    }
                } catch (UareUException e) {
                    log.error "Excepción al generar el FMD de la huella:", e
                }
            }

        }
    }

    /**
     * Calcula el FMD para el proceso de enrolamiento
     * @param format
     * @return
     */
    Engine.PreEnrollmentFmd GetFmd(Fmd.Format format) {
        Engine.PreEnrollmentFmd prefmd = null
        this.capturarHuella()
        if (model.captureResult != null) {
            if (Reader.CaptureQuality.CANCELED == model.captureResult.quality) {
                //capture canceled, return null
            } else if (null != model.captureResult.image && Reader.CaptureQuality.GOOD == model.captureResult.quality) {
                //acquire engine
                Engine engine = UareUGlobal.GetEngine()

                //extract features
                Fmd fmd = engine.CreateFmd(model.captureResult.image, Fmd.Format.ANSI_378_2004)

                //return prefmd
                prefmd = new Engine.PreEnrollmentFmd()
                prefmd.fmd = fmd
                prefmd.view_index = 0
            }
        }
        prefmd
    }

    /**
     * Captura una huella y actualiza la almacenada en el modelo
     */
    void capturarHuella() {

        model.captureResult = null

        if (!model.reader) {
            log.warn "No hay un lector seleccionado!"
            model.estado += "No hay un lector seleccionado!"
        } else {
            model.estado += "Apoye el dedo en el lector...\n"
            try {
                //wait for reader to become ready
                boolean bReady = false
                model.reader.Open(Reader.Priority.COOPERATIVE)
                while (!bReady) {
                    Reader.Status rs = model.reader.GetStatus()
                    if (Reader.ReaderStatus.BUSY == rs.status) {
                        //if busy, wait a bit
                        try {
                            Thread.sleep(100)
                        } catch (InterruptedException e) {
                            log.error "Lectura interrumpida!!!", e
                            model.estado += "Lectura interrumpida!!!"
                            break
                        }
                    } else if (Reader.ReaderStatus.READY == rs.status || Reader.ReaderStatus.NEED_CALIBRATION == rs.status) {
                        //ready for capture
                        bReady = true
                        break
                    } else {
                        //reader failure
                        log.error "Fallo en el lector!!!"
                        model.estado += "Fallo en el lector!!!"
                        break
                    }
                }

                if (bReady) {
                    //capture
                    model.captureResult = model.reader.Capture(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT, model.reader.GetCapabilities().resolutions[0], -1)
                    model.estado += "Calidad de la huella obtenida: ${model.captureResult.quality}...\n"
                }
                model.reader.Close()
            } catch (UareUException e) {
                log.error "Excepción al capturar huella:", e
            }
        }
    }

}
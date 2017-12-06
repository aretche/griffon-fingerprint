package io.github.aretche.griffonFingerprint

import griffon.core.artifact.GriffonService
import griffon.metadata.ArtifactProviderFor
import griffon.plugins.gsql.GsqlHandler

import javax.inject.Inject

@ArtifactProviderFor(GriffonService)
class HuellaStorageService {

    @Inject
    private GsqlHandler gsqlHandler

    @Inject
    private UsuarioStorageService usuarioStorageService

    List<Huella> list(Boolean eager = false) {
        gsqlHandler.withSql { dsName = 'default', sql ->
            List tmpList = []
            sql.eachRow('SELECT * FROM huellas') { h ->
                def huella = new Huella(
                        id:       h.id,
                        format:   h.format,
                        data:     h.data
                )
                if(eager && h.usuario_id){
                    huella.usuario = usuarioStorageService.get(h.usuario_id as Long)
                }
                tmpList << huella
            }
            tmpList
        }
    }


    Huella get(Long id) {
        log.info "Obteniendo huella con el id ${id}..."
        Huella respuesta
        gsqlHandler.withSql { dsName = 'default', sql ->
            sql.eachRow('SELECT * FROM huellas WHERE id = ?', [id], 0, 1) { us ->
                respuesta = new Huella(
                        id:       h.id,
                        format:   h.format,
                        data:     h.data
                )
                if(h.usuario_id){
                    respuesta.usuario = usuarioStorageService.get(h.usuario_id as Long)
                }
            }
        }
        respuesta
    }

    List<Huella> findAllByUsuario(Usuario usuario) {
        log.info "Obteniendo huellas del usuario ${usuario}..."
        List tmpList = []
        gsqlHandler.withSql { dsName = 'default', sql ->
            sql.eachRow('SELECT * FROM huellas WHERE usuario_id = ?', [usuario.id]) { h ->
                tmpList << new Huella(
                        id:       h.id,
                        format:   h.format,
                        data:     h.data,
                        usuario:  usuario
                )
            }
        }
        tmpList
    }

}

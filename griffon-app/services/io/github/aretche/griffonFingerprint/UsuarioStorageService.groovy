package io.github.aretche.griffonFingerprint

import griffon.core.artifact.GriffonService
import griffon.metadata.ArtifactProviderFor
import griffon.plugins.gsql.GsqlHandler

import javax.inject.Inject

@ArtifactProviderFor(GriffonService)
class UsuarioStorageService {

    @Inject
    private GsqlHandler gsqlHandler

    @Inject
    private HuellaStorageService huellaStorageService

    List<Usuario> list(Boolean eager = false) {
        gsqlHandler.withSql { dsName = 'default', sql ->
            List tmpList = []
            sql.eachRow('SELECT * FROM usuarios') { us ->
                Usuario usuario = new Usuario(
                        id:       us.id,
                        username: us.username,
                        email:    us.email
                )
                if(eager){
                    usuario.huellas = huellaStorageService.findAllByUsuario(usuario)
                }
                tmpList << usuario
            }
            tmpList
        }
    }


    Usuario get(Long id, Boolean eager = true) {
        log.info "Obteniendo usuario con el id ${id}..."
        Usuario respuesta
        gsqlHandler.withSql { dsName = 'default', sql ->
            List tmpList = []
            sql.eachRow('SELECT * FROM usuarios WHERE id = ?', [id], 0, 1) { us ->
                respuesta = new Usuario(
                        id:       us.id,
                        username: us.username,
                        email:    us.email
                )
                if(eager){
                    respuesta.huellas = huellaStorageService.findAllByUsuario(respuesta)
                }
            }
        }
        respuesta
    }

    Usuario findByUsername(String username, Boolean eager = true) {
        log.info "Obteniendo usuario con el username ${username}..."
        Usuario respuesta
        gsqlHandler.withSql { dsName = 'default', sql ->
            List tmpList = []
            sql.eachRow('SELECT * FROM usuarios WHERE username ILIKE ?', [username], 0, 1) { us ->
                respuesta = new Usuario(
                        id:       us.id,
                        username: us.username,
                        email:    us.email
                )
                if(eager){
                    respuesta.huellas = huellaStorageService.findAllByUsuario(respuesta)
                }
            }
        }
        respuesta
    }


    Usuario store(Usuario usuario) {
        if(usuario.id < 1) {
            // save
            gsqlHandler.withSql { dsName = 'default', sql ->
                String query = 'SELECT MAX(id) max FROM usuarios'
                usuario.id = (sql.firstRow(query).max as long) + 1
                sql.dataSet('usuarios').add(
                        id: usuario.id,
                        username: usuario.username,
                        email: usuario.email
                )
            }
        } else {
            // update
            gsqlHandler.withSql { dsName = 'default', sql ->
                sql.execute("""UPDATE usuarios
                                    SET username = ?, email = ? 
                                    WHERE id = ?""",
                                    [usuario.username, usuario.email, usuario.id])
            }
        }
        usuario
    }

    void remove(Usuario usuario) {
        gsqlHandler.withSql { dsName = 'default', sql ->
            sql.execute('DELETE FROM usuarios WHERE id = ?', [usuario.id])
        }
    }

    void dump() {
        gsqlHandler.withSql { dsName = 'default', sql ->
            sql.eachRow('SELECT * FROM usuarios') { rs ->
                println rs
            }
        }
    }
}

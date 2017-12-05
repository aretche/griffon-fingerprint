package io.github.aretche.griffonFingerprint

import griffon.plugins.gsql.GsqlBootstrap
import groovy.sql.Sql


import javax.inject.Named

@Named("default")
class DatabaseBootstrap implements GsqlBootstrap {
    @Override
    void init(String dataSourceName = 'default', Sql sql) {
        // operations after first connection to datasource
        def usuarios = sql.dataSet('usuarios')
        usuarios.add(
                id: 1,
                username: 'testuser',
                email: 'andres.almiray@canoo.com'
        )
    }

    @Override
    void destroy(String dataSourceName = 'default', Sql sql) {
        // operations before disconnecting from the datasource
    }
}

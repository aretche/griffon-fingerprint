dataSource {
    driverClassName = 'org.h2.Driver'
    username = 'sa'
    password = ''
    schema = false
    dbCreate = 'create'
    url = 'jdbc:h2:mem:griffon-fingerprint-internal'
    pool {
        idleTimeout = 60000
        maximumPoolSize = 8
        minimumIdle = 5
    }
}
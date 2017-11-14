application {
    title = 'griffon-fingerprint'
    startupGroups = ['griffonFingerprint']
    autoShutdown = true
}
mvcGroups {
    // MVC Group for "griffonFingerprint"
    'griffonFingerprint' {
        model      = 'io.github.aretche.griffonFingerprint.GriffonFingerprintModel'
        view       = 'io.github.aretche.griffonFingerprint.GriffonFingerprintView'
        controller = 'io.github.aretche.griffonFingerprint.GriffonFingerprintController'
    }
}
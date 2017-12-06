package io.github.aretche.griffonFingerprint

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Huella {

    Long    id
    String  format
    byte[]  data

    Usuario usuario

    static final List<String> PROPERTIES = ['format', 'data']

    String toString() { "${format}" }
}

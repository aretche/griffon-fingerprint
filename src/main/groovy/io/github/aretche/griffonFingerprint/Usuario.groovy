package io.github.aretche.griffonFingerprint

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Usuario {

    Long            id
    String          username
    String          email

    List<Huella>    huellas

    static final List<String> PROPERTIES = ['username', 'email']

    String toString() { "(${id}) ${username}" }
}

DROP TABLE IF EXISTS usuarios;
CREATE TABLE usuarios(id INTEGER NOT NULL PRIMARY KEY,
                      username VARCHAR(30) NOT NULL, email VARCHAR(100) NOT NULL);
DROP TABLE IF EXISTS huellas;
CREATE TABLE huellas(id INTEGER NOT NULL PRIMARY KEY,
                    usuario_id INTEGER NOT NULL, format VARCHAR(30) NOT NULL,
                    data BINARY NOT NULL);
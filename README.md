# Agenda de Citas

Agenda de citas con base de datos SQLite real, prepared statements y validación en
bucle.

**218 líneas** · Java SE + Swing + JDBC + SQLite

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white)

## Compilar y ejecutar

Necesita el driver JDBC de SQLite. Se baja de
[Maven Central](https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc):

```bash
curl -O https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar
javac -cp sqlite-jdbc-3.45.1.0.jar palada.java
java -cp .:sqlite-jdbc-3.45.1.0.jar palada
```

> En Windows, separa las rutas con `;` en lugar de `:`.

La base de datos `citas.db` se crea sola en el directorio de trabajo la primera vez que
se ejecuta el programa.

## Uso

Menú con cuatro opciones: crear una cita, buscar citas, ver todas las citas o salir.

## Esquema

```sql
CREATE TABLE IF NOT EXISTS citas (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre      TEXT NOT NULL,
    fecha       TEXT NOT NULL,
    descripcion TEXT NOT NULL
)
```

## Decisiones de diseño

**PreparedStatement en todas las escrituras.** El `INSERT` usa parámetros `?` en lugar
de concatenar cadenas, lo que lo hace inmune a inyección SQL.

**Try-with-resources** en el manejo de conexiones y statements, para que los recursos
se cierren siempre incluso si hay una excepción.

**Cancelar no deja registros a medias.** Los helpers `pedirTexto` y `pedirFecha`
devuelven `null` si el usuario cierra el diálogo con la X o pulsa Cancelar, y cada paso
propaga ese `null` con un `if (x == null) return;`. Si cancelas a la mitad del
formulario, no queda una cita guardada con campos vacíos.

**Validación en bucle.** En vez de aceptar la entrada y fallar después, los helpers
rechazan el dato inválido, muestran un ejemplo del formato correcto y vuelven a
preguntar.

**Sintaxis moderna:** switch expressions con flechas (`case "1" -> crearCita();`), que
requieren **Java 14 o superior**.

## Nota: búsqueda y ordenamiento

La búsqueda **no usa `LIKE` de SQL**. Carga todas las citas a memoria y filtra en Java
con `contains()` sobre `Cita.toString()`, que concatena nombre, fecha y descripción. Un
solo `contains` busca en los tres campos a la vez. Funciona bien para el volumen de
este proyecto, pero no escala: trae la tabla completa al heap en cada búsqueda.

Además, la fecha se guarda como `TEXT` en formato `dd/MM/yyyy HH:mm`, así que el
`ORDER BY fecha` ordena **alfabéticamente y no cronológicamente**. Si necesitas
ordenamiento correcto en el futuro, hay que guardar la fecha como `TEXT` en formato ISO
(`yyyy-MM-dd HH:mm`), que sí ordena bien como texto, o como tipo `DATE` nativo.

## Autor

**Jowell Acosta** — [GitHub](https://github.com/jowell2)

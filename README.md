# hm-database

Fellesbibliotek for databaseintegrasjon i DigiHoT.

NB! Flyttet til [https://github.com/navikt/hotlibs](https://github.com/navikt/hotlibs).

## Kom i gang

PostgreSQL:

```kotlin
dependencies {
    // Database
    implementation(libs.hm.database)
    implementation(libs.hm.database) {
        capabilities {
            requireCapability("no.nav.hjelpemidler:hm-database-postgresql")
        }
    }

    // Testing
    testImplementation(libs.hm.database) {
        capabilities {
            requireCapability("no.nav.hjelpemidler:hm-database-testcontainers")
        }
    }
}
```

Oracle:

```kotlin
dependencies {
    // Database
    implementation(libs.hm.database)
    implementation(libs.hm.database) {
        capabilities {
            requireCapability("no.nav.hjelpemidler:hm-database-oracle")
        }
    }

    // Testing
    testImplementation(libs.hm.database) {
        capabilities {
            requireCapability("no.nav.hjelpemidler:hm-database-h2")
        }
    }
}
```

### JsonMapper

Man kan definere sin egen JsonMapper for bruk i JSON-støtten til PostgreSQL. Dette gjøres via
en java.util.ServiceLoader.

Opprett følgende fil:

```text
src/main/resources/META-INF/services/no.nav.hjelpemidler.database.JsonMapper
```

Filen skal inneholde fully qualified name for en klasse som implmenterer interfacet no.nav.hjelpemidler.database.JsonMapper, e.g.:

```text
no.nav.hjelpemidler.app.MyJsonMapper
```

Eksempel på implementasjon:

```kotlin
class MyJsonMapper : JsonMapper {
    private val wrapped = jacksonMapperBuilder()
        .addModule(JavaTimeModule())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build()

    override fun <T> writeValueAsString(value: T): String =
        wrapped.writeValueAsString(value)

    override fun <T> readValue(content: String?, valueTypeRef: TypeReference<T>): T =
        wrapped.readValue(content, valueTypeRef)

    override fun <T> readValue(src: ByteArray?, valueTypeRef: TypeReference<T>): T =
        wrapped.readValue(src, valueTypeRef)

    override fun <T> convertValue(fromValue: Any?, toValueTypeRef: TypeReference<T>): T =
        wrapped.convertValue(fromValue, toValueTypeRef)
}
```

### Lage DataSource

```kotlin
// PostgreSQL
val dataSource = createDataSource(PostgreSQL) {
    envVarPrefix = "DB"
}

// Oracle
val dataSource = createDataSource(Oracle) {
    jdbcUrl = Configuration.DB_JDBC_URL
    username = Configuration.DB_USERNAME
    password = Configuration.DB_PASSWORD
    databaseName = Configuration.DB_NAME
}

// Testcontainers (PostgreSQL)
val dataSource = createDataSource(Testcontainers) {
    tag = "15-alpine"
}

// H2 (Oracle)
val dataSource = createDataSource(H2) {
    mode = H2Mode.Oracle
}
```

### Kjør Flyway

For å kjøre Flyway må følgende legges til i build.gradle.kts.

```kotlin
tasks.shadowJar { mergeServiceFiles() }
```

### Transaction

```kotlin
val result: Map<String, Any?> = transaction(dataSource) {
    it.update("INSERT INTO table_v1 (name) VALUES (:name)", mapOf("name" to "test"))
    it.single("SELECT * FROM table_v1 WHERE id = 1") { row: Row -> row.toMap() }
}
```

## Publisering

Publisering av hm-database gjøres manuelt pt.

Gå til "Releases" -> "Draft a new release" og skriv inn versjonen (e.g. "0.8.0") som skal bygges under "Choose a tag" og
trykk "Enter". Tag-en blir da opprettet og workflow for publisering kjører.

Lag ny release [her](https://github.com/navikt/hm-database/releases/new).

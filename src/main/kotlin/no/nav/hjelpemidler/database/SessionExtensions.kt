package no.nav.hjelpemidler.database

import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import org.intellij.lang.annotations.Language

typealias QueryParameters = Map<String, Any?>
typealias ResultMapper<T> = (Row) -> T?

fun <T> Session.query(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
    mapper: ResultMapper<T>,
): T? =
    single(queryOf(sql, queryParameters), mapper)

fun <T : Any> Session.single(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
    mapper: ResultMapper<T>,
): T =
    checkNotNull(query(sql, queryParameters, mapper)) {
        "Forventet en verdi, men var null"
    }

fun <T : Any> Session.queryList(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
    mapper: ResultMapper<T>,
): List<T> =
    list(queryOf(sql, queryParameters), mapper)

fun <T : Any> Session.queryPage(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
    limit: Int,
    offset: Int,
    totalNumberOfItemsLabel: String = "total",
    mapper: ResultMapper<T>,
): Page<T> {
    val limitParameter = prefix("limit")
    val offsetParameter = prefix("offset")
    var totalNumberOfItems = -1
    val items = list(
        queryOf(
            statement = """
                $sql
                LIMIT :$limitParameter
                OFFSET :$offsetParameter
            """.trimIndent(),
            paramMap = queryParameters + mapOf(
                limitParameter to limit + 1, // hent limit + 1 for å sjekke "hasMore"
                offsetParameter to offset,
            )
        )
    ) { row ->
        totalNumberOfItems = row.intOrNull(totalNumberOfItemsLabel) ?: -1
        mapper(row)
    }
    return Page(
        items = items.take(limit),
        total = totalNumberOfItems,
    )
}

fun Session.execute(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
): Boolean =
    execute(queryOf(sql, queryParameters))

fun Session.update(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
): UpdateResult =
    UpdateResult(actualRowCount = update(queryOf(sql, queryParameters)))

fun Session.updateAndReturnGeneratedKey(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
): Long =
    checkNotNull(updateAndReturnGeneratedKey(queryOf(sql, queryParameters))) {
        "Forventet en generert nøkkel, men var null"
    }

fun Session.batch(
    @Language("PostgreSQL") sql: String,
    queryParameters: Collection<QueryParameters> = emptyList(),
): List<Int> =
    batchPreparedNamedStatement(sql, queryParameters)

fun Session.batchAndReturnGeneratedKeys(
    @Language("PostgreSQL") sql: String,
    queryParameters: Collection<QueryParameters> = emptyList(),
): List<Long> =
    batchPreparedNamedStatementAndReturnGeneratedKeys(sql, queryParameters)

fun <T : Any> Session.batch(
    @Language("PostgreSQL") sql: String,
    items: Collection<T> = emptyList(),
    block: (T) -> QueryParameters,
): List<Int> =
    batch(sql, items.map(block))

fun <T : Any> Collection<T>.batch(
    tx: Session,
    @Language("PostgreSQL") sql: String,
    block: (T) -> QueryParameters,
): List<Int> =
    tx.batch(sql, map(block))

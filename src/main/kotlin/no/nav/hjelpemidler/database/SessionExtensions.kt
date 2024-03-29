package no.nav.hjelpemidler.database

import no.nav.hjelpemidler.database.sql.Sql
import org.intellij.lang.annotations.Language

typealias Session = kotliquery.Session
typealias ResultMapper<T> = (Row) -> T?

fun <T : Any> Session.single(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
    mapper: ResultMapper<T>,
): T = query(sql, queryParameters, mapper) ?: throw NoSuchElementException("Forventet en verdi, men var null")

fun <T : Any> Session.single(
    sql: Sql,
    queryParameters: QueryParameters = emptyMap(),
    mapper: ResultMapper<T>,
): T = single(sql.toString(), queryParameters, mapper)

fun <T> Session.query(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
    mapper: ResultMapper<T>,
): T? = single(queryOf(sql, queryParameters), mapper)

fun <T> Session.query(
    sql: Sql,
    queryParameters: QueryParameters = emptyMap(),
    mapper: ResultMapper<T>,
): T? = query(sql.toString(), queryParameters, mapper)

fun <T : Any> Session.queryList(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
    mapper: ResultMapper<T>,
): List<T> = list(queryOf(sql, queryParameters), mapper)

fun <T : Any> Session.queryList(
    sql: Sql,
    queryParameters: QueryParameters = emptyMap(),
    mapper: ResultMapper<T>,
): List<T> = queryList(sql.toString(), queryParameters, mapper)

fun <T : Any> Session.queryPage(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
    limit: Int,
    offset: Int,
    totalNumberOfItemsLabel: String = "total",
    mapper: ResultMapper<T>,
): Page<T> {
    val limitParameter = "no_nav_hjelpemidler_database_limit"
    val offsetParameter = "no_nav_hjelpemidler_database_offset"
    var totalNumberOfItems = -1
    val items = list(
        queryOf(
            statement = """
                $sql
                LIMIT :$limitParameter
                OFFSET :$offsetParameter
            """.trimIndent(),
            queryParameters + mapOf(
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

fun <T : Any> Session.queryPage(
    sql: Sql,
    queryParameters: QueryParameters = emptyMap(),
    limit: Int,
    offset: Int,
    totalNumberOfItemsLabel: String = "total",
    mapper: ResultMapper<T>,
): Page<T> = queryPage(sql.toString(), queryParameters, limit, offset, totalNumberOfItemsLabel, mapper)

fun Session.execute(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
): Boolean = execute(queryOf(sql, queryParameters))

fun Session.execute(sql: Sql, queryParameters: QueryParameters = emptyMap()): Boolean =
    execute(sql.toString(), queryParameters)

fun Session.update(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
): UpdateResult = UpdateResult(update(queryOf(sql, queryParameters)))

fun Session.update(sql: Sql, queryParameters: QueryParameters = emptyMap()): UpdateResult =
    update(sql.toString(), queryParameters)

fun Session.updateAndReturnGeneratedKey(
    @Language("PostgreSQL") sql: String,
    queryParameters: QueryParameters = emptyMap(),
): Long = checkNotNull(updateAndReturnGeneratedKey(queryOf(sql, queryParameters))) {
    "Forventet en generert nøkkel, men var null"
}

fun Session.updateAndReturnGeneratedKey(sql: Sql, queryParameters: QueryParameters = emptyMap()): Long =
    updateAndReturnGeneratedKey(sql.toString(), queryParameters)

fun Session.batch(
    @Language("PostgreSQL") sql: String,
    queryParameters: Collection<QueryParameters> = emptyList(),
): List<Int> = batchPreparedNamedStatement(sql, queryParameters.prepare())

fun Session.batch(sql: Sql, queryParameters: Collection<QueryParameters> = emptyList()): List<Int> =
    batch(sql.toString(), queryParameters)

fun <T : Any> Session.batch(
    @Language("PostgreSQL") sql: String,
    items: Collection<T> = emptyList(),
    block: (T) -> QueryParameters,
): List<Int> = batch(sql, items.map(block))

fun <T : Any> Session.batch(sql: Sql, items: Collection<T> = emptyList(), block: (T) -> QueryParameters): List<Int> =
    batch(sql.toString(), items, block)

fun Session.batchAndReturnGeneratedKeys(
    @Language("PostgreSQL") sql: String,
    queryParameters: Collection<QueryParameters> = emptyList(),
): List<Long> = batchPreparedNamedStatementAndReturnGeneratedKeys(sql, queryParameters.prepare())

fun Session.batchAndReturnGeneratedKeys(
    sql: Sql,
    queryParameters: Collection<QueryParameters> = emptyList(),
): List<Long> = batchAndReturnGeneratedKeys(sql.toString(), queryParameters)

fun <T : Any> Session.batchAndReturnGeneratedKeys(
    @Language("PostgreSQL") sql: String,
    items: Collection<T> = emptyList(),
    block: (T) -> QueryParameters,
): List<Long> = batchAndReturnGeneratedKeys(sql, items.map(block))

fun <T : Any> Session.batchAndReturnGeneratedKeys(
    sql: Sql,
    items: Collection<T> = emptyList(),
    block: (T) -> QueryParameters
): List<Long> = batchAndReturnGeneratedKeys(sql.toString(), items, block)

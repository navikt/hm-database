package no.nav.hjelpemidler.database

import kotliquery.Session
import javax.sql.DataSource

interface StoreContext<X : Any> {
    val dataSource: DataSource

    fun createTransactionContext(tx: Session): X

    operator fun invoke(tx: Session): X = createTransactionContext(tx)
}

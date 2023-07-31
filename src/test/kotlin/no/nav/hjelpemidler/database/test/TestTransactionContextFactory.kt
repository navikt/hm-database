package no.nav.hjelpemidler.database.test

import kotliquery.Session
import no.nav.hjelpemidler.database.TransactionContextFactory
import java.sql.Connection

class TestTransactionContextFactory : TransactionContextFactory<TestTransactionContext> {
    override val connection: Connection
        get() = testDataSource.connection

    override operator fun invoke(session: Session): TestTransactionContext =
        object : TestTransactionContext {
            override val testStore: TestStore = TestStore(session)
        }
}

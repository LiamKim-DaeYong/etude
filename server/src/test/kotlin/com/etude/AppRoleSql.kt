package com.etude

import java.sql.Connection
import java.util.UUID
import javax.sql.DataSource

/**
 * 애플리케이션 역할(`etude_app`)로 Hibernate를 거치지 않고 SQL을 실행한다. RLS만으로 막히는지 확인할 때 쓴다.
 *
 * [tenantId]가 있으면 애플리케이션처럼 트랜잭션 안에서 `app.tenant_id`를 설정한다. 블록이 끝나면 커밋한다.
 */
fun <T> DataSource.inTransaction(tenantId: UUID? = null, block: (Connection) -> T): T = connection.use { connection ->
    connection.autoCommit = false
    try {
        if (tenantId != null) {
            connection.prepareStatement("select set_config('app.tenant_id', ?, true)").use {
                it.setString(1, tenantId.toString())
                it.execute()
            }
        }
        block(connection).also { connection.commit() }
    } catch (ex: Throwable) {
        connection.rollback()
        throw ex
    }
}

/** 첫 행의 첫 칸. */
fun Connection.queryValue(sql: String, vararg args: Any?): Any? = prepareStatement(sql).use { statement ->
    args.forEachIndexed { i, arg -> statement.setObject(i + 1, arg) }
    statement.executeQuery().use { if (it.next()) it.getObject(1) else null }
}

fun Connection.count(sql: String, vararg args: Any?): Long = (queryValue(sql, *args) as Number).toLong()

fun Connection.update(sql: String, vararg args: Any?): Int = prepareStatement(sql).use { statement ->
    args.forEachIndexed { i, arg -> statement.setObject(i + 1, arg) }
    statement.executeUpdate()
}

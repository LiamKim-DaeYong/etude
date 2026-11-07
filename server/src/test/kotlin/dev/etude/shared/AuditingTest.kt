package dev.etude.shared

import dev.etude.IntegrationTest
import dev.etude.inventory.domain.Stock
import dev.etude.inventory.domain.StockRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@IntegrationTest
class AuditingTest(
    @Autowired private val stockRepository: StockRepository,
    @Autowired private val transaction: TransactionTemplate,
) {

    @Test
    fun `records creation time and updates only the modification time`() {
        val productId = UUID.randomUUID()
        val created = transaction.execute { stockRepository.save(Stock(productId, 10)) }!!

        assertThat(created.createdAt).isNotNull()
        assertThat(created.updatedAt).isEqualTo(created.createdAt)

        Thread.sleep(10)
        transaction.executeWithoutResult { stockRepository.findByProductId(productId)!!.decrease(1) }

        val updated = transaction.execute { stockRepository.findByProductId(productId) }!!
        assertThat(updated.createdAt).isEqualTo(created.createdAt)
        assertThat(updated.updatedAt).isAfter(created.updatedAt)
    }
}

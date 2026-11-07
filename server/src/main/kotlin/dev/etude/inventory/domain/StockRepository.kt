package dev.etude.inventory.domain

import org.springframework.data.repository.Repository
import java.util.UUID

interface StockRepository : Repository<Stock, UUID> {

    fun save(stock: Stock): Stock

    fun findByProductId(productId: UUID): Stock?
}

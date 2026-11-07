package dev.etude.inventory.application

import dev.etude.catalog.ProductCatalog
import dev.etude.inventory.application.StockRejectedException.Reason
import dev.etude.inventory.domain.Stock
import dev.etude.inventory.domain.StockRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class StockService(
    private val stockRepository: StockRepository,
    private val productCatalog: ProductCatalog,
) {

    @Transactional
    fun register(productId: UUID, quantity: Int): StockDetails {
        productCatalog.find(productId)
            ?: throw StockRejectedException(Reason.PRODUCT_NOT_FOUND, "Product not found: $productId")

        if (stockRepository.findByProductId(productId) != null) {
            throw StockRejectedException(Reason.STOCK_ALREADY_REGISTERED, "Stock already registered: $productId")
        }

        return StockDetails.from(stockRepository.save(Stock(productId, quantity)))
    }

    fun getStock(productId: UUID): StockDetails? = stockRepository.findByProductId(productId)?.let(StockDetails::from)
}

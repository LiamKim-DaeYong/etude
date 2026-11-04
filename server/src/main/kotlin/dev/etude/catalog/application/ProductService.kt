package dev.etude.catalog.application

import dev.etude.catalog.ProductCatalog
import dev.etude.catalog.ProductInfo
import dev.etude.catalog.domain.Product
import dev.etude.catalog.domain.ProductRepository
import dev.etude.shared.Money
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ProductService(
    private val productRepository: ProductRepository,
) : ProductCatalog {

    @Transactional
    fun register(name: String, price: Money): ProductDetails = ProductDetails.from(productRepository.save(Product(name, price)))

    fun getProduct(id: UUID): ProductDetails? = productRepository.findByIdOrNull(id)?.let(ProductDetails::from)

    override fun find(id: UUID): ProductInfo? = productRepository.findByIdOrNull(id)?.let {
        ProductInfo(
            id = requireNotNull(it.id),
            name = it.name,
            price = it.price,
            orderable = it.isOrderable(),
        )
    }
}

package com.etude.catalog.application

import com.etude.catalog.ProductCatalog
import com.etude.catalog.ProductInfo
import com.etude.catalog.domain.Product
import com.etude.catalog.domain.ProductRepository
import com.etude.shared.Money
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

    fun findProduct(id: UUID): ProductDetails? = productRepository.findById(id)?.let(ProductDetails::from)

    override fun find(id: UUID): ProductInfo? = productRepository.findById(id)?.let {
        ProductInfo(
            id = requireNotNull(it.id),
            name = it.name,
            price = it.price,
            orderable = it.isOrderable(),
        )
    }
}

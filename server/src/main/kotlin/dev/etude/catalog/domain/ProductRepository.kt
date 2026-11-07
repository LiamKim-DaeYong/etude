package dev.etude.catalog.domain

import org.springframework.data.repository.Repository
import java.util.UUID

interface ProductRepository : Repository<Product, UUID> {

    fun save(product: Product): Product

    fun findById(id: UUID): Product?
}

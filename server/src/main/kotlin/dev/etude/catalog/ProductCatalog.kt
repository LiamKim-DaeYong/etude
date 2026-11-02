package dev.etude.catalog

import java.util.UUID

interface ProductCatalog {
    fun find(id: UUID): ProductInfo?
}

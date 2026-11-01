package dev.etude

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EtudeApplication

fun main(args: Array<String>) {
    runApplication<EtudeApplication>(*args)
}

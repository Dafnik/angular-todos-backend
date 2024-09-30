package me.dafnik.angular_todos_backend

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") // Allow CORS for all endpoints
            .allowedOrigins("http://localhost:8080", "*") // Allow all origins
            .allowedMethods("*") // Allow all methods (GET, POST, etc.)
            .allowedHeaders("*") // Allow all headers
    }
}

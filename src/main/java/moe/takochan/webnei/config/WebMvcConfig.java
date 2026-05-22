package moe.takochan.webnei.config;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final ClassPathResource SPA_INDEX = new ClassPathResource("/static/index.html");
    private static final List<String> SPA_EXCLUDED_PREFIXES = List.of("api/", "actuator/", "assets/", "static/");

    private final CorsProperties corsProperties;

    public WebMvcConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .resourceChain(true)
                .addResolver(new SpaResourceResolver());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = corsProperties.devOrigins().toArray(String[]::new);
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "HEAD", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    private static final class SpaResourceResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            Resource requested = super.getResource(resourcePath, location);
            if (requested != null) {
                return requested;
            }
            if (resourcePath != null) {
                String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
                for (String excluded : SPA_EXCLUDED_PREFIXES) {
                    if (normalized.startsWith(excluded)) {
                        return null;
                    }
                }
                // Avoid serving index.html for explicit static asset file requests (path with an extension).
                int lastSlash = normalized.lastIndexOf('/');
                String lastSegment = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
                if (lastSegment.contains(".")) {
                    return null;
                }
            }
            return SPA_INDEX.exists() ? SPA_INDEX : null;
        }
    }
}

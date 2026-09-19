package Farme_rich;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Existing: serve uploaded files
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/"); // relative to project root

        // Angular SPA (dolphin-naturals, formerly wiseGrocer) — serve real assets,
        // fall back to index.html for any client-side route so refresh/deep-links work.
        //
        // NOTE: the bare directory root ("/dolphin-naturals" and "/dolphin-naturals/")
        // is NOT handled here. Spring's ResourceHttpRequestHandler rejects an empty
        // resourcePath via StringUtils.hasText(path) before this resolver chain ever
        // runs, so no PathResourceResolver override can intercept it. That case is
        // handled directly in SpaForwardController via a controller-level forward,
        // which is matched before the resource handler is ever consulted.
        registry.addResourceHandler("/dolphin-naturals/**")
                .addResourceLocations("classpath:/static/dolphin-naturals/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {

                        // Defensive only — in practice this branch is unreachable for
                        // requests to the bare root, since ResourceHttpRequestHandler
                        // rejects an empty resourcePath before invoking any resolver.
                        // Kept in case a future Spring version changes that guard, or
                        // a resource transformer ever passes an empty path through.
                        if (resourcePath.isEmpty() || resourcePath.endsWith("/")) {
                            return new ClassPathResource("/static/dolphin-naturals/index.html");
                        }

                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested; // real file: main.js, styles.css, assets/logo.png, etc.
                        }
                        return new ClassPathResource("/static/dolphin-naturals/index.html");
                    }
                });

        // Angular SPA (purest) — same pattern as dolphin-naturals above.
        // Bare root ("/purest", "/purest/") is intentionally NOT handled here;
        // see SpaForwardController for that case, per the same guard-order
        // reasoning documented above for dolphin-naturals.
        registry.addResourceHandler("/purest/**")
                .addResourceLocations("classpath:/static/purest/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {

                        if (resourcePath.isEmpty() || resourcePath.endsWith("/")) {
                            return new ClassPathResource("/static/purest/index.html");
                        }

                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        return new ClassPathResource("/static/purest/index.html");
                    }
                });

        // --- Old wiseGrocer handler ---
        // Kept here commented out in case you need the old URL to keep resolving
        // during a transition period. Delete once the rename is fully rolled out.
        //
        // registry.addResourceHandler("/wiseGrocer/**")
        //         .addResourceLocations("classpath:/static/wiseGrocer/")
        //         .resourceChain(true)
        //         .addResolver(new PathResourceResolver() {
        //             @Override
        //             protected Resource getResource(String resourcePath, Resource location) throws IOException {
        //                 if (resourcePath.isEmpty() || resourcePath.endsWith("/")) {
        //                     return new ClassPathResource("/static/wiseGrocer/index.html");
        //                 }
        //                 Resource requested = location.createRelative(resourcePath);
        //                 if (requested.exists() && requested.isReadable()) {
        //                     return requested;
        //                 }
        //                 return new ClassPathResource("/static/wiseGrocer/index.html");
        //             }
        //         });
    }
}

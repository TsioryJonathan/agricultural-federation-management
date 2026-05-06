package com.hei.agriculturalfederationmanagement.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RestController
public class SpecificationController {

    @GetMapping(value = "/api-spec", produces = "text/yaml")
    public ResponseEntity<String> getSpecification() {
        try {
            ClassPathResource resource = new ClassPathResource("spec.yaml");
            String content = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);

            // Replace https with http so Swagger UI can make requests
            content = content.replace("https://localhost:8080", "http://localhost:8080");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/yaml"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=spec.yaml")
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to load specification: " + e.getMessage());
        }
    }

    @GetMapping("/swagger")
    public ResponseEntity<String> getSwaggerUI() {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <title>Agricultural Federation API - Swagger UI</title>
                <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css" />
                <style>
                    html { box-sizing: border-box; overflow: -moz-scrollbars-vertical; overflow-y: scroll; }
                    *, *:before, *:after { box-sizing: inherit; }
                    body { margin: 0; padding: 0; }
                    .topbar { display: none; }
                </style>
            </head>
            <body>
                <div id="swagger-ui"></div>
                <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js" crossorigin></script>
                <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-standalone-preset.js" crossorigin></script>
                <script>
                    window.onload = () => {
                        window.ui = SwaggerUIBundle({
                            url: "/api-spec",
                            dom_id: '#swagger-ui',
                            presets: [
                                SwaggerUIBundle.presets.apis,
                                SwaggerUIStandalonePreset
                            ],
                            layout: "StandaloneLayout",
                            deepLinking: true,
                            // Force HTTP scheme
                            requestInterceptor: (req) => {
                                if (req.url.startsWith('https://localhost')) {
                                    req.url = req.url.replace('https://localhost', 'http://localhost');
                                }
                                return req;
                            }
                        });
                    };
                </script>
            </body>
            </html>
            """;

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
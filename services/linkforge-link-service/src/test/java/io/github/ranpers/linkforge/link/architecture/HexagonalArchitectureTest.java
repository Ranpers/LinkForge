package io.github.ranpers.linkforge.link.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HexagonalArchitectureTest {

    private static final List<String> DOMAIN_FORBIDDEN_IMPORTS = List.of(
            ".application.",
            ".adapter.",
            ".config.",
            ".infrastructure.",
            "org.springframework.",
            "org.apache.kafka.",
            "org.apache.ibatis.",
            "com.mybatisflex."
    );

    private static final List<String> APPLICATION_FORBIDDEN_IMPORTS = List.of(
            ".adapter.",
            ".config.",
            ".infrastructure.",
            "org.apache.kafka.",
            "org.apache.ibatis.",
            "com.mybatisflex.",
            "org.springframework.web."
    );

    @Test
    void domainMustNotDependOnOuterLayersOrFrameworks() throws IOException {
        assertNoForbiddenImports("/domain/", DOMAIN_FORBIDDEN_IMPORTS);
    }

    @Test
    void applicationMustNotDependOnAdaptersOrTechnicalFrameworks() throws IOException {
        assertNoForbiddenImports("/application/", APPLICATION_FORBIDDEN_IMPORTS);
    }

    private void assertNoForbiddenImports(String layer, List<String> forbiddenImports)
            throws IOException {
        List<String> violations = new ArrayList<>();
        Path sourceRoot = findSourceRoot();
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".java")).toList()) {
                String normalizedPath = path.toString().replace('\\', '/');
                String relativePath = sourceRoot.relativize(path).toString().replace('\\', '/');
                if (!isLayer(relativePath, layer)) {
                    continue;
                }
                for (String line : Files.readAllLines(path)) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("import ")
                            && forbiddenImports.stream().anyMatch(trimmed::contains)) {
                        violations.add(normalizedPath + ": " + trimmed);
                    }
                }
            }
        }
        assertTrue(violations.isEmpty(), () -> "六边形依赖方向违规:\n" + String.join("\n", violations));
    }

    private static boolean isLayer(String relativePath, String layer) {
        String layerName = layer.replace("/", "");
        String[] segments = relativePath.split("/");
        for (int index = 1; index < segments.length - 1; index++) {
            if (segments[index].equals(layerName)) {
                return true;
            }
        }
        return false;
    }

    private Path findSourceRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            Path moduleRoot = current.resolve("src/main/java/io/github/ranpers/linkforge/link");
            if (Files.isDirectory(moduleRoot)) {
                return moduleRoot;
            }
            Path reactorRoot = current.resolve(
                    "services/linkforge-link-service/src/main/java/io/github/ranpers/linkforge/link"
            );
            if (Files.isDirectory(reactorRoot)) {
                return reactorRoot;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("找不到 Link 主源码目录");
    }
}

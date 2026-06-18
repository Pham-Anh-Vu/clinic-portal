package com.company.clinicportal.tools;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ListDocPlaceholdersTool {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{[^}]+}");

    public static void main(String[] args) throws Exception {
        Path doc = Path.of("src/main/resources/reports/29. Benh an ngoai tru PHCN-in.doc");
        try (InputStream in = Files.newInputStream(doc);
             HWPFDocument hwpf = new HWPFDocument(in);
             WordExtractor extractor = new WordExtractor(hwpf)) {
            Set<String> found = new LinkedHashSet<>();
            Matcher m = PLACEHOLDER.matcher(extractor.getText());
            while (m.find()) {
                found.add(m.group());
            }
            System.out.println("=== PLACEHOLDER TRONG FILE .DOC (" + found.size() + ") ===");
            found.forEach(System.out::println);
        }
    }
}

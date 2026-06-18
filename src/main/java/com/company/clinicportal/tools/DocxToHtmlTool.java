package com.company.clinicportal.tools;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.convert.out.html.HtmlExporterNonXSLT;
import org.docx4j.model.images.ConversionImageHandler;
import org.w3c.dom.Document;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * CLI tool: converts Word .docx to UTF-8 HTML.
 *
 * Usage:
 *   gradlew exportToDieuTriTemplateToHtml
 *   OR
 *   java ... com.company.clinicportal.tools.DocxToHtmlTool <input.docx> <output.html>
 */
public final class DocxToHtmlTool {
    private DocxToHtmlTool() {
    }

    public static void main(String[] args) throws Exception {
        WordprocessingMLPackage wordMLPackage;
        Path output;

        if (args.length >= 2) {
            Path input = toPath(args[0]).toAbsolutePath().normalize();
            output = toPath(args[1]).toAbsolutePath().normalize();
            if (!Files.exists(input)) {
                throw new IllegalArgumentException("Input file not found: " + input);
            }
            Files.createDirectories(output.getParent());
            wordMLPackage = WordprocessingMLPackage.load(input.toFile());
        } else {
            // Default mode for this project (avoids passing Vietnamese filenames via CLI args on Windows)
            String resourcePath = "/reports/tờ điều trị BN BCB.docx";
            // Write to a separate file so it doesn't overwrite the printable HTML template.
            output = Path.of("src/main/resources/reports/tờ điều trị BN BCB.converted.html")
                    .toAbsolutePath()
                    .normalize();
            Files.createDirectories(output.getParent());

            try (InputStream in = DocxToHtmlTool.class.getResourceAsStream(resourcePath)) {
                if (in == null) {
                    throw new IllegalArgumentException("Resource not found: " + resourcePath);
                }
                wordMLPackage = WordprocessingMLPackage.load(in);
            }
        }

        // Keep it self-contained: no image export for now (templates mostly text/tables).
        // If your DOCX has images, we can extend this to export images to a folder and reference them.
        ConversionImageHandler imageHandler = null;
        HtmlExporterNonXSLT exporter = new HtmlExporterNonXSLT(wordMLPackage, imageHandler);

        Document doc = exporter.export();
        String html = toHtmlString(doc);
        String css = exporter.getCss();
        if (css != null && !css.isBlank()) {
            html = injectCss(html, css);
        }
        html = enforceGlobalFont(html, "Times New Roman");
        html = ensureUtf8Meta(html);

        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(output.toFile()))) {
            out.write(html.getBytes(StandardCharsets.UTF_8));
        }

        System.out.println("Wrote HTML: " + output);
    }

    private static Path toPath(String arg) {
        // Supports either plain file path or file:// URI (recommended for non-ASCII filenames on Windows).
        String trimmed = arg != null ? arg.trim() : "";
        if (trimmed.startsWith("file:")) {
            return Path.of(URI.create(trimmed));
        }
        return Path.of(trimmed);
    }

    private static String ensureUtf8Meta(String html) {
        String lower = html.toLowerCase();
        int headIdx = lower.indexOf("<head");
        if (headIdx < 0) return html;

        int headClose = lower.indexOf(">", headIdx);
        if (headClose < 0) return html;

        String meta = "\n<meta charset=\"UTF-8\" />\n";
        return html.substring(0, headClose + 1) + meta + html.substring(headClose + 1);
    }

    private static String toHtmlString(Document htmlDoc) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            transformer.transform(new DOMSource(htmlDoc), new StreamResult(out));
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    private static String injectCss(String html, String css) {
        String lower = html.toLowerCase();
        int headIdx = lower.indexOf("<head");
        if (headIdx < 0) return html;
        int headClose = lower.indexOf(">", headIdx);
        if (headClose < 0) return html;

        String style = "\n<style type=\"text/css\">\n" + css + "\n</style>\n";
        return html.substring(0, headClose + 1) + style + html.substring(headClose + 1);
    }

    private static String enforceGlobalFont(String html, String fontFamily) {
        String css = """
                @media print {
                    html, body, body * {
                        font-family: '%s', serif !important;
                    }
                }
                html, body, body * {
                    font-family: '%s', serif !important;
                }
                """.formatted(fontFamily, fontFamily);
        return injectCss(html, css);
    }
}


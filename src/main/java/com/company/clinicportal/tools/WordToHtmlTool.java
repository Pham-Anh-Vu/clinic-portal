package com.company.clinicportal.tools;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * CLI tool: converts legacy Word .doc to UTF-8 HTML.
 *
 * Usage:
 *   gradlew exportWordTemplateToHtml
 *   OR
 *   java ... com.company.clinicportal.tools.WordToHtmlTool <input.doc> <output.html>
 */
public final class WordToHtmlTool {
    private WordToHtmlTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: WordToHtmlTool <input.doc> <output.html>");
            System.exit(2);
            return;
        }

        Path input = Path.of(args[0]).toAbsolutePath().normalize();
        Path output = Path.of(args[1]).toAbsolutePath().normalize();

        if (!Files.exists(input)) {
            throw new IllegalArgumentException("Input file not found: " + input);
        }
        Files.createDirectories(output.getParent());

        String html;
        try (InputStream in = new FileInputStream(input.toFile())) {
            HWPFDocument doc = new HWPFDocument(in);
            html = convertToHtml(doc);
        }

        html = ensureUtf8Meta(html);

        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(output.toFile()))) {
            out.write(html.getBytes(StandardCharsets.UTF_8));
        }

        System.out.println("Wrote HTML: " + output);
    }

    private static String convertToHtml(HWPFDocument wordDoc) throws Exception {
        Document htmlDocument = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .newDocument();

        WordToHtmlConverter converter = new WordToHtmlConverter(htmlDocument);
        converter.processDocument(wordDoc);
        Document w3c = converter.getDocument();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            transformer.transform(new DOMSource(w3c), new StreamResult(out));
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    private static String ensureUtf8Meta(String html) {
        // WordToHtmlConverter often emits <meta http-equiv="Content-Type" ...>.
        // Force an explicit UTF-8 meta charset near <head> for consistent browsers.
        String lower = html.toLowerCase();
        int headIdx = lower.indexOf("<head");
        if (headIdx < 0) return html;

        int headClose = lower.indexOf(">", headIdx);
        if (headClose < 0) return html;

        String meta = "\n<meta charset=\"UTF-8\" />\n";
        return html.substring(0, headClose + 1) + meta + html.substring(headClose + 1);
    }
}


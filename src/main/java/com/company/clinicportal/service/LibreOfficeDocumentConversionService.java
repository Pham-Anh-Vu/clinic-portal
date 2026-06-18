package com.company.clinicportal.service;

import io.jmix.reports.libintegration.JmixOfficeIntegration;
import io.jmix.reports.yarg.formatters.impl.xls.DocumentConverter;
import io.jmix.reports.yarg.formatters.impl.xls.DocumentConverterImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Chuyển tài liệu Word (.doc/.docx) sang PDF qua LibreOffice (Jmix Reports).
 */
@Service
public class LibreOfficeDocumentConversionService {

    private final JmixOfficeIntegration officeIntegration;

    public LibreOfficeDocumentConversionService(
            @Qualifier("report_OfficeIntegration") JmixOfficeIntegration officeIntegration) {
        this.officeIntegration = officeIntegration;
    }

    public byte[] convertDocumentToPdf(byte[] documentBytes) throws IOException {
        DocumentConverter converter = new DocumentConverterImpl(officeIntegration);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            converter.convertToPdf(DocumentConverter.FileType.DOCUMENT, documentBytes, out);
            return out.toByteArray();
        }
    }
}

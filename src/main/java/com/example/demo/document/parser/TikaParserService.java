package com.example.demo.document.parser;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;

@Service
public class TikaParserService {

    private static final Logger log = LoggerFactory.getLogger(TikaParserService.class);
    private final Tika tika;

    public TikaParserService(Tika tika) {
        this.tika = tika;
    }

    /**
     * Detects the MIME type of an input stream using Apache Tika's magic number detection.
     * The input stream is marked and reset to ensure it is not consumed in the process.
     *
     * @param inputStream the document's input stream
     * @return the detected MIME type, e.g., "application/pdf"
     */
    public String detectMimeType(InputStream inputStream) {
        if (inputStream == null) {
            log.warn("Null InputStream provided for MIME type detection.");
            return "application/octet-stream";
        }
        try {
            InputStream streamToUse = inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
            if (streamToUse.markSupported()) {
                streamToUse.mark(8192); // 8KB is plenty for magic numbers
                String mimeType = tika.detect(streamToUse);
                streamToUse.reset();
                return mimeType;
            } else {
                return tika.detect(streamToUse);
            }
        } catch (Exception e) {
            log.warn("Failed to detect MIME type: {}", e.getMessage());
            return "application/octet-stream";
        }
    }

    /**
     * Extracts text from the given InputStream of a document (e.g., PDF or DOCX).
     *
     * @param inputStream the document's input stream
     * @return the extracted raw text, or an empty string if parsing fails
     */
    public String parse(InputStream inputStream) {
        if (inputStream == null) {
            log.warn("Null InputStream provided to TikaParserService, returning empty string.");
            return "";
        }
        try {
            return tika.parseToString(inputStream);
        } catch (Exception e) {
            log.warn("Failed to extract text from document input stream: {}", e.getMessage());
            return "";
        }
    }
}

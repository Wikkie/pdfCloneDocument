package io.github.wikkie;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;


public class PdfClone {

    private final static Logger log = LoggerFactory.getLogger(PdfClone.class);

    public static void main(String[] args) {
        try {
            log.info("running {}", "Main");
            if (args.length != 2) {
                throw new Exception("two args needed: arg-0: source (pdf), arg-1: destination (pdf)");
            }

            PdfClone.copyTo(args[0], args[1]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyTo(String source, String pdfDestination) {
        try {
            PDDocument document = PDDocument.load(new File(source));
            for (int i = 0; i < document.getNumberOfPages(); i++) {

                log.info("Processing Page {}", i);
                PDPage currentPage = document.getPage(i);

                PDFStreamParser sourceParser = new PDFStreamParser(currentPage);
                sourceParser.parse();
                List<Object> sourceTokens = sourceParser.getTokens()
                        .stream()
                        .filter(o -> true) // opportunity to filter elements from content stream
                        .collect(Collectors.toList());

                PDStream destStream = new PDStream(document);

                try (OutputStream outputStream = destStream.createOutputStream(COSName.FLATE_DECODE)) {
                    ContentStreamWriter contentStreamWriter = new ContentStreamWriter(outputStream);
                    contentStreamWriter.writeTokens(sourceTokens);
                }

                currentPage.setContents(destStream);
            }

            document.save(pdfDestination);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

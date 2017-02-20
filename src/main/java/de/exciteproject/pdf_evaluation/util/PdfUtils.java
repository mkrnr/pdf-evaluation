package de.exciteproject.pdf_evaluation.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;

public class PdfUtils {

    public static void main(String[] args) {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        for (File inputFile : inputDir.listFiles()) {
            try {
                PdfUtils.removeFirstNPages(1, inputFile,
                        new File(outputDir.getAbsolutePath() + File.separator + inputFile.getName()));
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method based on: http://stackoverflow.com/a/26569544
     */
    public static void removeFirstNPages(int n, File inputFile, File outputFile)
            throws FileNotFoundException, DocumentException, IOException {
        PdfReader reader = new PdfReader(inputFile.getAbsolutePath());
        int pages_number = reader.getNumberOfPages();
        if (pages_number > 0) {
            Document document = new Document(reader.getPageSizeWithRotation(1));
            PdfCopy copy = new PdfCopy(document, new FileOutputStream(outputFile));
            document.open();
            for (int i = n + 1; i <= pages_number; i++) {
                PdfImportedPage page = copy.getImportedPage(reader, i);
                copy.addPage(page);
            }
            document.close();
        }
    }

}

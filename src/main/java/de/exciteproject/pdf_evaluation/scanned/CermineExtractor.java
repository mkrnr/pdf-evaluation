package de.exciteproject.pdf_evaluation.scanned;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.exciteproject.refext.util.FileUtils;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

public class CermineExtractor {
    public static void main(String[] args) throws IOException, TimeoutException, AnalysisException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        for (File inputFile : FileUtils.listFilesRecursively(inputDir)) {
            ContentExtractor extractor = new ContentExtractor();
            System.out.println(inputFile);
            InputStream inputStream = new FileInputStream(inputFile);
            extractor.setPDF(inputStream);
            try {
                String fullText = extractor.getRawFullText();
                System.out.println(fullText.length());
                org.apache.commons.io.FileUtils.writeStringToFile(
                        new File(outputDir.getAbsolutePath() + File.separator + inputFile.getName()), fullText);
            } catch (Exception e) {
                System.out.println("null");
                org.apache.commons.io.FileUtils.writeStringToFile(
                        new File(outputDir.getAbsolutePath() + File.separator + inputFile.getName()), "");
            }
        }
    }
}

package de.exciteproject.pdf_evaluation.refextract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.bibref.model.BibEntry;
import pl.edu.icm.cermine.configuration.ExtractionConfigBuilder;
import pl.edu.icm.cermine.configuration.ExtractionConfigRegister;
import pl.edu.icm.cermine.exception.AnalysisException;

public class CermineReferenceExtractor {

    public static void main(String[] args) throws AnalysisException, IOException {

        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        File configurationFile = new File(args[2]);

        // load configuration file specifying the retrained SVM models for zone
        // classification

        for (File inputFile : inputDir.listFiles()) {
            ExtractionConfigBuilder builder = new ExtractionConfigBuilder();
            builder.addConfiguration(configurationFile.getAbsolutePath());
            ExtractionConfigRegister.set(builder.buildConfiguration());

            ContentExtractor extractor = new ContentExtractor();

            File outputFile = new File(
                    outputDir.getAbsolutePath() + File.separator + inputFile.getName().replaceAll("\\.pdf", ".txt"));
            InputStream inputStream = new FileInputStream(inputFile);
            extractor.setPDF(inputStream);
            List<BibEntry> result = extractor.getReferences();
            List<String> references = new ArrayList<String>();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            if (result == null) {
                System.out.println("result empty");
                bufferedWriter.close();
                continue;
            }
            for (BibEntry bibEntry : result) {
                String referenceString = bibEntry.getText();
                references.add(referenceString);
            }

            for (String reference : references) {
                bufferedWriter.write(reference);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();

        }

    }

}

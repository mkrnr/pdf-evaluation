package de.exciteproject.pdf_evaluation.refextract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.exciteproject.refext.extract.CermineLineLayoutExtractor;
import de.exciteproject.refext.extract.CrfReferenceLineAnnotator;
import pl.edu.icm.cermine.ComponentConfiguration;
import pl.edu.icm.cermine.exception.AnalysisException;

public class RefextReferenceExtractor {

    public static void main(String[] args) throws IOException, AnalysisException {

        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        File modelFile = new File(args[2]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        CrfReferenceLineAnnotator crfReferenceLineAnnotator = new CrfReferenceLineAnnotator(modelFile);

        // TODO: add trained cermine models!!!!!
        ComponentConfiguration componentConfiguration = new ComponentConfiguration();
        CermineLineLayoutExtractor cermineLineLayoutExtractor = new CermineLineLayoutExtractor(componentConfiguration);

        for (File inputFile : inputDir.listFiles()) {

            File outputFile = new File(
                    outputDir.getAbsolutePath() + File.separator + inputFile.getName().split("\\.")[0] + ".csv");
            if (outputFile.exists()) {
                continue;
            }

            List<String> layoutLines = cermineLineLayoutExtractor.extract(inputFile);
            List<String> annotatedLines = crfReferenceLineAnnotator.annotate(layoutLines);

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            for (String annotatedLine : annotatedLines) {
                if (annotatedLine.startsWith("B-REF") || annotatedLine.startsWith("I-REF")) {
                    bufferedWriter.write(annotatedLine);
                    bufferedWriter.newLine();
                }
            }
            bufferedWriter.close();

        }
    }

}

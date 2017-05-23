package de.exciteproject.pdf_evaluation.refextract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.grobid.core.data.BibDataSet;
import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;

public class GrobidReferenceExtractor {

    public static void main(String[] args) throws IOException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        File grobidHomeDir = new File(args[2]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        GrobidReferenceExtractor grobidReferenceExtractor = new GrobidReferenceExtractor(grobidHomeDir);
        for (File inputFile : inputDir.listFiles()) {
            File outputFile = new File(
                    outputDir.getAbsolutePath() + File.separator + inputFile.getName().split("\\.")[0] + ".csv");

            // TODO remove
            if (outputFile.exists()) {
                continue;
            }
            try {
                // List<String> references =
                // grobidReferenceExtractor.extractFromReferenceStrings(inputFile);
                List<String> references = grobidReferenceExtractor.extractAnnotatedLinesFromPDF(inputFile);
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
                for (String reference : references) {
                    bufferedWriter.write(reference);
                    bufferedWriter.newLine();
                }
                bufferedWriter.close();
            } catch (StackOverflowError e) {
                System.err.println("stackoverglow at file: " + inputFile.getAbsolutePath());
            }
        }

    }

    private File grobidHomeDir;

    public GrobidReferenceExtractor(File grobidHomeDir) {
        this.grobidHomeDir = grobidHomeDir;
    }

    /*
     * TODO:refactor
     */
    public List<String> extractAnnotatedLinesFromPDF(File pdfFile) {
        List<String> references = new ArrayList<String>();
        try {
            File grobidPropertiesFile = new File(
                    this.grobidHomeDir + File.separator + "config" + File.separator + "grobid.properties");

            MockContext.setInitialContext(this.grobidHomeDir.getAbsolutePath(), grobidPropertiesFile.getAbsolutePath());
            GrobidProperties.getInstance();

            System.out.println(">>>>>>>> GROBID_HOME=" + GrobidProperties.get_GROBID_HOME_PATH());

            Engine engine = GrobidFactory.getInstance().createEngine();

            List<BibDataSet> tei = engine.processReferences(pdfFile, false);
            for (BibDataSet bibDS : tei) {
                String reference = bibDS.getRawBib().toString();
                reference = "B-REF\t" + reference;
                reference = reference.replaceAll("\n", "\nI-REF\t");
                reference = reference.replaceAll("\\s*\n", "\n");

                reference = reference.replaceAll("@BULLET", "•");

                references.add(reference);
            }
        } catch (Exception e) {
            // If an exception is generated, print a stack trace
            e.printStackTrace();
        } finally {
            try {
                MockContext.destroyInitialContext();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return references;
    }

}

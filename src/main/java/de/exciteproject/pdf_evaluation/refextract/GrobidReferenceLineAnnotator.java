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

public class GrobidReferenceLineAnnotator extends ReferenceLineAnnotator {

    public static void main(String[] args) throws IOException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        File grobidHomeDir = new File(args[2]);
        File trainingModelDirectory = new File(args[3]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        GrobidReferenceLineAnnotator grobidReferenceExtractor = new GrobidReferenceLineAnnotator(grobidHomeDir);
        for (File inputFile : inputDir.listFiles()) {
            File outputFile = new File(
                    outputDir.getAbsolutePath() + File.separator + inputFile.getName().split("\\.")[0] + ".csv");

            try {
                // List<String> references =
                // grobidReferenceExtractor.extractFromReferenceStrings(inputFile);
                List<String> references = grobidReferenceExtractor.extractAnnotatedReferenceLinesFromPDF(inputFile,
                        trainingModelDirectory);
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

    public GrobidReferenceLineAnnotator(File grobidHomeDir) {
        this.grobidHomeDir = grobidHomeDir;
    }

    public List<String> extractAnnotatedReferenceLinesFromPDF(File pdfFile, File trainingModelsDirectory)
            throws IOException {
        this.initializeModels(trainingModelsDirectory);
        return this.annotateReferenceLinesFromPDF(pdfFile);
    }

    @Override
    public List<String> annotateReferenceLinesFromPDF(File pdfFile) throws IOException {
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

                String[] referenceSplit = reference.split("\\n");
                for (String referenceLine : referenceSplit) {
                    references.add(referenceLine);
                }
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

    protected void copyModelsToHome(File trainingModelDirectory) throws IOException {

        String[] modelDirectoryNames = { "segmentation", "reference-segmenter" };

        File modelTargetDirectory = new File(this.grobidHomeDir + File.separator + "models");

        for (String modelDirectoryName : modelDirectoryNames) {
            // run training of segmentation
            File currentModelSourceFile = new File(
                    trainingModelDirectory + File.separator + modelDirectoryName + File.separator + "model.wapiti");
            File currentModelTargetFile = new File(
                    modelTargetDirectory + File.separator + modelDirectoryName + File.separator + "model.wapiti");
            org.apache.commons.io.FileUtils.copyFile(currentModelSourceFile, currentModelTargetFile);
        }

    }

    @Override
    public void initializeModels(File trainingModelsDirectory) throws IOException {
        this.copyModelsToHome(trainingModelsDirectory);
    }

}

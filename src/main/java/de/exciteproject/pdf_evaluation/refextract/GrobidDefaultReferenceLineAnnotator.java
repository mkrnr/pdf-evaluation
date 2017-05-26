package de.exciteproject.pdf_evaluation.refextract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GrobidDefaultReferenceLineAnnotator extends GrobidReferenceLineAnnotator {

    public static void main(String[] args) throws IOException {
        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);
        File grobidHomeDir = new File(args[2]);
        File defaultTrainingModelDir = new File(args[3]);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        GrobidDefaultReferenceLineAnnotator grobidReferenceExtractor = new GrobidDefaultReferenceLineAnnotator(
                grobidHomeDir, defaultTrainingModelDir);
        for (File inputFile : inputDir.listFiles()) {
            File outputFile = new File(
                    outputDir.getAbsolutePath() + File.separator + inputFile.getName().split("\\.")[0] + ".csv");

            try {
                // List<String> references =
                // grobidReferenceExtractor.extractFromReferenceStrings(inputFile);
                List<String> references = grobidReferenceExtractor.extractAnnotatedReferenceLinesFromPDF(inputFile,
                        null);
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

    private File defaultModelDir;

    public GrobidDefaultReferenceLineAnnotator(File grobidHomeDir, File defaultModelDir) {
        super(grobidHomeDir);
        this.defaultModelDir = defaultModelDir;
    }

    @Override
    public void initializeModels(File trainingModelsDirectory) throws IOException {
        this.copyModelsToHome(trainingModelsDirectory);
    }

    /**
     * Ignore the passed trainingModelDirectory and instead copy the files from
     * this.defaultModelDir
     */
    @Override
    protected void copyModelsToHome(File trainingModelDirectory) throws IOException {
        String[] modelDirectoryNames = { "segmentation", "reference-segmenter" };

        File modelTargetDirectory = new File(this.grobidHomeDir + File.separator + "models");

        for (String modelDirectoryName : modelDirectoryNames) {
            // run training of segmentation
            File currentModelSourceFile = new File(
                    this.defaultModelDir + File.separator + modelDirectoryName + File.separator + "model.wapiti");
            File currentModelTargetFile = new File(
                    modelTargetDirectory + File.separator + modelDirectoryName + File.separator + "model.wapiti");
            org.apache.commons.io.FileUtils.copyFile(currentModelSourceFile, currentModelTargetFile);
        }

    }

}

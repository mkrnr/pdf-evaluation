package de.exciteproject.pdf_evaluation.refextract.train;

import java.io.File;
import java.io.IOException;

import org.grobid.core.mock.MockContext;
import org.grobid.trainer.TrainerRunner;

import de.exciteproject.pdf_evaluation.util.FileUtils;

public class GrobidRefExtractTrainer extends RefExtractTrainer {

    public static void main(String[] args) throws Exception {
        File grobidHomeDirectory = new File(args[0]);
        File trainingSourceDirectory = new File(args[1]);
        File trainingTargetDirectory = new File(args[2]);

        GrobidRefExtractTrainer grobidRefExtractTrainer = new GrobidRefExtractTrainer(grobidHomeDirectory,
                trainingTargetDirectory);
        grobidRefExtractTrainer.train(trainingSourceDirectory);
    }

    private File grobidHomeDirectory;
    private File trainingTargetDirectory;

    public GrobidRefExtractTrainer(File grobidHomeDirectory, File trainingTargetDirectory) {
        this.grobidHomeDirectory = grobidHomeDirectory;
        this.trainingTargetDirectory = trainingTargetDirectory;
    }

    public void train(File trainingFilesDirectory) throws Exception {
        this.copyTrainingFiles(trainingFilesDirectory);

        String[] modelDirectoryNames = { "segmentation", "reference-segmenter" };
        for (String modelDirectoryName : modelDirectoryNames) {
            // run training of segmentation
            String[] trainingArguments = { "0", modelDirectoryName, "-gH", this.grobidHomeDirectory.getAbsolutePath() };

            TrainerRunner.main(trainingArguments);

            File modelSourceDirectory = new File(grobidHomeDirectory + File.separator + "models");
            File modelTargetDirectory = new File(trainingTargetDirectory + File.separator + "models");

            File currentModelSourceFile = new File(
                    modelSourceDirectory + File.separator + modelDirectoryName + File.separator + "model.wapiti");
            File currentModelTargetFile = new File(
                    modelTargetDirectory + File.separator + modelDirectoryName + File.separator + "model.wapiti");
            org.apache.commons.io.FileUtils.copyFile(currentModelSourceFile, currentModelTargetFile);
            System.out.println("DONE");

            // destroys the inital context that was created by TrainerRunner
            MockContext.destroyInitialContext();
        }

    }

    private void copyTrainingFiles(File trainingFilesDirectory) throws IOException {
        File datasetDirectory = new File(this.grobidHomeDirectory.getParentFile() + File.separator + "grobid-trainer"
                + File.separator + "resources" + File.separator + "dataset");
        File segmentationCorpusDirectory = new File(
                datasetDirectory + File.separator + "segmentation" + File.separator + "corpus");

        // this "reference-segmenter" is the default folder of grobid....
        File referenceSegmentationCorpusDirectory = new File(
                datasetDirectory + File.separator + "reference-segmenter" + File.separator + "corpus");

        File segmentationTrainingDirectory = new File(trainingFilesDirectory + File.separator + "segmentation");
        File referenceSegmentationTrainingDirectory = new File(
                trainingFilesDirectory + File.separator + "reference-segmentation");

        this.copySubFolder(segmentationTrainingDirectory, segmentationCorpusDirectory, "raw");
        this.copySubFolder(segmentationTrainingDirectory, segmentationCorpusDirectory, "tei");
        this.copySubFolder(referenceSegmentationTrainingDirectory, referenceSegmentationCorpusDirectory, "raw");
        this.copySubFolder(referenceSegmentationTrainingDirectory, referenceSegmentationCorpusDirectory, "tei");

    }

    private void copySubFolder(File sourceDirectory, File targetDirectory, String subFolderName) throws IOException {
        FileUtils.copyAndReplaceInDir(new File(sourceDirectory + File.separator + subFolderName),
                new File(targetDirectory + File.separator + subFolderName));
    }

}

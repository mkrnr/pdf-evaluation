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

        GrobidRefExtractTrainer grobidRefExtractTrainer = new GrobidRefExtractTrainer(grobidHomeDirectory);
        grobidRefExtractTrainer.train(trainingSourceDirectory, trainingTargetDirectory);
    }

    private File grobidHomeDirectory;

    public GrobidRefExtractTrainer(File grobidHomeDirectory) {
        this.grobidHomeDirectory = grobidHomeDirectory;
    }

    @Override
    public void train(File trainingFilesDirectory, File trainingTargetDirectory) throws Exception {
        this.copyTrainingFiles(trainingFilesDirectory);

        String[] modelDirectoryNames = { "segmentation", "reference-segmenter" };
        File modelSourceDirectory = new File(this.grobidHomeDirectory + File.separator + "models");
        File modelTargetDirectory = new File(trainingTargetDirectory + File.separator);

        for (String modelDirectoryName : modelDirectoryNames) {
            // run training of segmentation
            String[] trainingArguments = { "0", modelDirectoryName, "-gH", this.grobidHomeDirectory.getAbsolutePath() };

            TrainerRunner.main(trainingArguments);

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

    private void copySubFolder(File sourceDirectory, File targetDirectory, String subFolderName) throws IOException {
        FileUtils.copyAndReplaceInDir(new File(sourceDirectory + File.separator + subFolderName),
                new File(targetDirectory + File.separator + subFolderName));
    }

    private void copyTrainingFiles(File trainingFilesDirectory) throws IOException {
        File datasetDirectory = new File(this.grobidHomeDirectory.getParentFile() + File.separator + "grobid-trainer"
                + File.separator + "resources" + File.separator + "dataset");

        String[] modelDirectoryNames = { "segmentation", "reference-segmenter" };

        for (String modelDirectoryName : modelDirectoryNames) {

            File currentCorpusDirectory = new File(
                    datasetDirectory + File.separator + modelDirectoryName + File.separator + "corpus");

            File currentTrainingDirectory = new File(trainingFilesDirectory + File.separator + modelDirectoryName);

            this.copySubFolder(currentTrainingDirectory, currentCorpusDirectory, "raw");
            this.copySubFolder(currentTrainingDirectory, currentCorpusDirectory, "tei");
        }
    }

}

package de.exciteproject.pdf_evaluation.refextract.train;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.cli.ParseException;

import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.libsvm.training.SVMBodyBuilder;
import pl.edu.icm.cermine.libsvm.training.SVMInitialBuilder;
import pl.edu.icm.cermine.libsvm.training.SVMMetadataBuilder;

public class CermineRefExtractTrainer extends RefExtractTrainer {

    public static void main(String[] args)
            throws IOException, InterruptedException, ParseException, AnalysisException, CloneNotSupportedException {
        File trainingSourceDirectory = new File(args[0]);
        File trainingTargetDirectory = new File(args[1]);

        CermineRefExtractTrainer cermineRefExtractTrainer = new CermineRefExtractTrainer(trainingTargetDirectory);
        cermineRefExtractTrainer.train(trainingSourceDirectory);
    }

    private File trainingTargetDirectory;

    /**
     * 
     * @param homeDirectory:
     *            the directory that CERMINE accesses during training
     */
    public CermineRefExtractTrainer(File trainingTargetDirectory) {
        this.trainingTargetDirectory = trainingTargetDirectory;
    }

    public void train(File trainingSourceDirectory)
            throws IOException, InterruptedException, ParseException, AnalysisException, CloneNotSupportedException {

        // run training for metadata, body, and category models

        File modelMetadataFile = new File(
                trainingTargetDirectory.getAbsolutePath() + File.separator + "model-metadata");
        String[] command = { "-input", trainingSourceDirectory.getAbsolutePath(), "-output",
                modelMetadataFile.getAbsolutePath() };
        SVMMetadataBuilder.main(command);

        File modelBodyFile = new File(trainingTargetDirectory.getAbsolutePath() + File.separator + "model-body");
        command[3] = modelBodyFile.getAbsolutePath();
        SVMBodyBuilder.main(command);

        File modelCategoryFile = new File(
                trainingTargetDirectory.getAbsolutePath() + File.separator + "model-category");
        command[3] = modelCategoryFile.getAbsolutePath();
        SVMInitialBuilder.main(command);

        // write config file
        File configFile = new File(trainingTargetDirectory.getAbsolutePath() + File.separator + "cermine.properties");
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(configFile));

        this.addConfigLines("zoneClassifier.initial", modelCategoryFile, bufferedWriter);
        this.addConfigLines("zoneClassifier.metadata", modelMetadataFile, bufferedWriter);
        this.addConfigLines("contentFilter", modelBodyFile, bufferedWriter);

        bufferedWriter.close();

    }

    private void addConfigLines(String configName, File modelFile, BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write(configName + ".model=" + modelFile.getAbsolutePath());
        bufferedWriter.newLine();
        bufferedWriter.write(configName + ".ranges=" + modelFile.getAbsolutePath() + ".range");
        bufferedWriter.newLine();

    }

}

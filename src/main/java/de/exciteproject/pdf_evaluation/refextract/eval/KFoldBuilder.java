package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

public abstract class KFoldBuilder {

    protected int k;
    protected File idFile;

    public KFoldBuilder(int k, File idFile) {
        this.k = k;
        this.idFile = idFile;
    }

    public abstract void build(int i, File sourceDirectory, File targetDirectory) throws IOException;

    protected void buildDirectory(int i, File inputDirectory, File outputDirectory) throws IOException {
        if (!inputDirectory.exists()) {
            inputDirectory.mkdirs();
        }
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        KFoldDataset kFoldDataset = new KFoldDataset(this.k);
        kFoldDataset.build(this.idFile, inputDirectory);
        List<File> trainingFilesToCopy = kFoldDataset.getTrainingFold(i);
        System.out.println(trainingFilesToCopy.size());
        FileUtils.cleanDirectory(outputDirectory);
        for (File trainingFileToCopy : trainingFilesToCopy) {
            FileUtils.copyFileToDirectory(trainingFileToCopy, outputDirectory);
        }
    }

}

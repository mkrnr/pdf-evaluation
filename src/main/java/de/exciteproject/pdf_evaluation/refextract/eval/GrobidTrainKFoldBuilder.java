package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.File;
import java.io.IOException;

public class GrobidTrainKFoldBuilder extends KFoldBuilder {

    public static void main(String[] args) throws IOException {
        int k = Integer.parseInt(args[0]);
        File idFile = new File(args[1]);
        File sourceDirectory = new File(args[2]);
        File targetDirectory = new File(args[3]);
        GrobidTrainKFoldBuilder grobidKFoldBuilder = new GrobidTrainKFoldBuilder(k, idFile);
        grobidKFoldBuilder.build(0, sourceDirectory, targetDirectory);
    }

    public GrobidTrainKFoldBuilder(int k, File idFile) throws IOException {
        super(k, idFile);
    }

    /**
     * Generate the files for fold i in the targetDirectory. Deletes existing
     * files in the targetDirectory
     * 
     * @param i:
     *            current fold, starting at zero
     * @throws IOException
     */
    public void build(int i, File sourceDirectory, File targetDirectory) throws IOException {
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }

        this.buildTrainingDirectory(i, new File(sourceDirectory + File.separator + "segmentation"),
                new File(targetDirectory + File.separator + "segmentation"));
        this.buildTrainingDirectory(i, new File(sourceDirectory + File.separator + "reference-segmentation"),
                new File(targetDirectory + File.separator + "reference-segmentation"));

    }

    private void buildTrainingDirectory(int i, File inputTrainingDirectory, File outputTrainingDirectory)
            throws IOException {
        this.buildDirectory(i, new File(inputTrainingDirectory + File.separator + "raw"),
                new File(outputTrainingDirectory + File.separator + "raw"));
        this.buildDirectory(i, new File(inputTrainingDirectory + File.separator + "tei"),
                new File(outputTrainingDirectory + File.separator + "tei"));

    }

}

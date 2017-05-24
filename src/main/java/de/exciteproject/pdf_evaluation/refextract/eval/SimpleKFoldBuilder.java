package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.File;
import java.io.IOException;

public class SimpleKFoldBuilder extends KFoldBuilder {

    public static void main(String[] args) throws IOException {
        int k = Integer.parseInt(args[0]);
        File idFile = new File(args[1]);
        File sourceDirectory = new File(args[2]);
        File targetDirectory = new File(args[3]);
        SimpleKFoldBuilder simpleKFoldBuilder = new SimpleKFoldBuilder(k, idFile);
        simpleKFoldBuilder.build(0, sourceDirectory, targetDirectory);
    }

    public SimpleKFoldBuilder(int k, File idFile) throws IOException {
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
    @Override
    public void build(int i, File sourceDirectory, File targetDirectory) throws IOException {
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }

        this.buildDirectory(i, sourceDirectory, targetDirectory);
    }

}

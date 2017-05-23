package de.exciteproject.pdf_evaluation.refextract.train;

import java.io.File;

public abstract class RefExtractTrainer {

    public abstract void train(File trainingSourceDirectory, File trainingTargetDirectory) throws Exception;
}

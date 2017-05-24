package de.exciteproject.pdf_evaluation.refextract;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class ReferenceLineAnnotator {

    public abstract List<String> annotateReferenceLinesFromPDF(File pdfFile) throws IOException;

    public abstract void initializeModels(File trainingModelsDirectory) throws IOException;

}

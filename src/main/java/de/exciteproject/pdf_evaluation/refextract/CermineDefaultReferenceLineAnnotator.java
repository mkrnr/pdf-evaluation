package de.exciteproject.pdf_evaluation.refextract;

import java.io.File;
import java.io.IOException;

public class CermineDefaultReferenceLineAnnotator extends CermineReferenceLineAnnotator {

    @Override
    public void initializeModels(File trainingModelsDirectory) throws IOException {
        this.configurationFile = new File("pl/edu/icm/cermine/application-default.properties");
    }

}

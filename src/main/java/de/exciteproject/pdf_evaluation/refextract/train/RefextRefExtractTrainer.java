package de.exciteproject.pdf_evaluation.refextract.train;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.ParseException;

import com.cybozu.labs.langdetect.LangDetectException;

import cc.mallet.fst.CRF;
import cc.mallet.types.InstanceList;
import de.exciteproject.refext.train.ReferenceExtractorTrainer;
import pl.edu.icm.cermine.exception.AnalysisException;

public class RefextRefExtractTrainer extends RefExtractTrainer {

    public static void main(String[] args)
            throws IOException, InterruptedException, ParseException, AnalysisException, LangDetectException {
        File trainingSourceDirectory = new File(args[0]);
        File trainingTargetDirectory = new File(args[1]);
        List<String> featureNames = Arrays.asList(args[2].split(","));

        RefextRefExtractTrainer refextRefExtractTrainer = new RefextRefExtractTrainer(featureNames);
        refextRefExtractTrainer.train(trainingSourceDirectory, trainingTargetDirectory);
    }

    private List<String> featureNames;

    /**
     *
     * @param trainingTargetDirectory:
     *            the directory that CERMINE accesses during training
     */
    public RefextRefExtractTrainer(List<String> featureNames) {
        this.featureNames = featureNames;
    }

    @Override
    public void train(File trainingSourceDirectory, File trainingTargetDirectory)
            throws IOException, InterruptedException, ParseException, AnalysisException, LangDetectException {

        // run training for metadata, body, and category models
        ReferenceExtractorTrainer referenceExtractorTrainer = new ReferenceExtractorTrainer(this.featureNames);
        InstanceList trainingInstances = referenceExtractorTrainer.buildInstanceListFromDir(trainingSourceDirectory);

        referenceExtractorTrainer.addStartState();
        referenceExtractorTrainer.addStatesForThreeQuarterLabelsConnectedAsIn(trainingInstances);
        // referenceExtractorTrainer.setCRFTrainerByLabelLikelihood(10.0);
        referenceExtractorTrainer.setCRFTrainerByL1LabelLikelihood(20.0);
        // referenceExtractorTrainer.setCRFTrainerByL1LabelLikelihood(0.75);

        CRF crf = referenceExtractorTrainer.train(trainingInstances, trainingInstances);
        File modelOutputFile = new File(trainingTargetDirectory + File.separator + "model.ser");
        crf.write(modelOutputFile);
    }

}

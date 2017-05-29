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
        List<String> replacements = Arrays.asList(args[3].split(","));
        List<String> conjunctions = Arrays.asList(args[4].split(","));
        double gaussianPriorVariance = Double.parseDouble(args[5]);

        RefextRefExtractTrainer refextRefExtractTrainer = new RefextRefExtractTrainer(featureNames, replacements,
                conjunctions, gaussianPriorVariance, "ThreeQuarterLabels", "ByL1LabelLikelihood");
        refextRefExtractTrainer.train(trainingSourceDirectory, trainingTargetDirectory);
    }

    private List<String> featureNames;
    private List<String> replacements;
    private List<String> conjunctions;
    private double trainerWeight;
    private String addStatesName;
    private String trainerName;

    /**
     *
     * @param trainingTargetDirectory:
     *            the directory that CERMINE accesses during training
     */
    public RefextRefExtractTrainer(List<String> featureNames, List<String> replacements, List<String> conjunctions,
            double trainerWeight, String addStatesName, String trainerName) {
        this.featureNames = featureNames;
        this.replacements = replacements;
        this.conjunctions = conjunctions;
        this.trainerWeight = trainerWeight;
        this.addStatesName = addStatesName;
        this.trainerName = trainerName;
    }

    @Override
    public void train(File trainingSourceDirectory, File trainingTargetDirectory)
            throws IOException, InterruptedException, ParseException, AnalysisException, LangDetectException {

        // run training for metadata, body, and category models
        ReferenceExtractorTrainer referenceExtractorTrainer = new ReferenceExtractorTrainer(this.featureNames,
                this.replacements, this.conjunctions);
        InstanceList trainingInstances = referenceExtractorTrainer.buildInstanceListFromDir(trainingSourceDirectory);

        referenceExtractorTrainer.crf.addStartState();
        switch (this.addStatesName) {
        case "ThreeQuarterLabels":
            referenceExtractorTrainer.crf.addStatesForThreeQuarterLabelsConnectedAsIn(trainingInstances);
            break;
        case "BiLabels":
            referenceExtractorTrainer.crf.addStatesForBiLabelsConnectedAsIn(trainingInstances);
            break;
        case "Labels":
            referenceExtractorTrainer.crf.addStatesForLabelsConnectedAsIn(trainingInstances);
            break;
        case "HalfLabels":
            referenceExtractorTrainer.crf.addStatesForHalfLabelsConnectedAsIn(trainingInstances);
            break;
        }

        switch (this.trainerName) {
        case "ByLabelLikelihood":
            referenceExtractorTrainer.setCRFTrainerByLabelLikelihood(this.trainerWeight);
            break;
        case "ByL1LabelLikelihood":
            referenceExtractorTrainer.setCRFTrainerByL1LabelLikelihood(this.trainerWeight);
            break;
        }

        CRF crf = referenceExtractorTrainer.train(trainingInstances, trainingInstances);
        File modelOutputFile = new File(trainingTargetDirectory + File.separator + "model.ser");
        crf.write(modelOutputFile);
    }

}

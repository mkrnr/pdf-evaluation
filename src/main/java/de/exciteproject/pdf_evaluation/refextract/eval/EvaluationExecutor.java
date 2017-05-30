package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.exciteproject.pdf_evaluation.refextract.CermineDefaultReferenceLineAnnotator;
import de.exciteproject.pdf_evaluation.refextract.CermineReferenceLineAnnotator;
import de.exciteproject.pdf_evaluation.refextract.GrobidDefaultReferenceLineAnnotator;
import de.exciteproject.pdf_evaluation.refextract.GrobidReferenceLineAnnotator;
import de.exciteproject.pdf_evaluation.refextract.ParsCitReferenceLineAnnotator;
import de.exciteproject.pdf_evaluation.refextract.ReferenceLineAnnotator;
import de.exciteproject.pdf_evaluation.refextract.RefextReferenceLineAnnotator;
import de.exciteproject.pdf_evaluation.refextract.train.CermineRefExtractTrainer;
import de.exciteproject.pdf_evaluation.refextract.train.GrobidRefExtractTrainer;
import de.exciteproject.pdf_evaluation.refextract.train.RefExtractTrainer;
import de.exciteproject.pdf_evaluation.refextract.train.RefextRefExtractTrainer;
import de.exciteproject.refext.util.FileUtils;

public class EvaluationExecutor {

    public static void main(String[] args) throws Exception {
        int mode = Integer.parseInt(args[0]);
        boolean train = Boolean.parseBoolean(args[1]);
        int k = Integer.parseInt(args[2]);
        File idFile = new File(args[3]);
        File trainingSourceDirectory = new File(args[4]);
        File foldTargetDirectory = new File(args[5]);
        File pdfDirectory = new File(args[6]);
        File annotatedFilesDirectory = new File(args[7]);
        int evaluationMode = Integer.parseInt(args[8]);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        Date currentDate = new Date();

        if (!foldTargetDirectory.getName().matches(".*\\d\\d\\d\\d-\\d\\d-\\d\\d_\\d\\d-\\d\\d-\\d\\d-\\d\\d\\d")) {
            foldTargetDirectory = new File(
                    foldTargetDirectory.getAbsolutePath() + "_" + dateFormat.format(currentDate));
        }

        KFoldBuilder trainFoldBuilder = null;

        KFoldDataset testKFoldDataset = new KFoldDataset(k);
        testKFoldDataset.build(idFile, pdfDirectory);
        RefExtractTrainer refExtractTrainer = null;
        ReferenceLineAnnotator referenceLineAnnotator = null;
        ReferenceEvaluator referenceEvaluator = new ReferenceEvaluator();
        switch (mode) {
        case 1:
            File grobidHomeDirectory = new File(args[9]);
            trainFoldBuilder = new GrobidTrainKFoldBuilder(k, idFile);
            refExtractTrainer = new GrobidRefExtractTrainer(grobidHomeDirectory);
            referenceLineAnnotator = new GrobidReferenceLineAnnotator(grobidHomeDirectory);
            break;
        case 2:
            grobidHomeDirectory = new File(args[9]);
            File defaultModelDirectory = new File(args[10]);
            trainFoldBuilder = new GrobidTrainKFoldBuilder(k, idFile);
            // throws NullPointerException when train=true
            refExtractTrainer = null;
            referenceLineAnnotator = new GrobidDefaultReferenceLineAnnotator(grobidHomeDirectory,
                    defaultModelDirectory);
            break;
        case 3:
            trainFoldBuilder = new SimpleKFoldBuilder(k, idFile);
            refExtractTrainer = new CermineRefExtractTrainer();
            referenceLineAnnotator = new CermineReferenceLineAnnotator();
            break;
        case 4:
            // File defaultCermineConfigurationFile = new File(args[8]);
            trainFoldBuilder = new SimpleKFoldBuilder(k, idFile);
            // throws NullPointerException when train=true
            refExtractTrainer = null;
            referenceLineAnnotator = new CermineDefaultReferenceLineAnnotator();
            break;
        case 5:
            List<String> features = Arrays.asList(args[9].split(","));
            List<String> conjunctions = Arrays.asList(args[10].split(","));

            double gaussianPriorVariance = Double.parseDouble(args[11]);
            String addStatesName = args[12];
            String trainerName = args[13];
            List<String> replacements = new ArrayList<String>();
            if (args.length > 14) {
                replacements = Arrays.asList(args[14].split(","));
            }

            trainFoldBuilder = new SimpleKFoldBuilder(k, idFile);
            refExtractTrainer = new RefextRefExtractTrainer(features, replacements, conjunctions, gaussianPriorVariance,
                    addStatesName, trainerName);
            referenceLineAnnotator = new RefextReferenceLineAnnotator();
            break;

        case 6:
            File citeExtractFile = new File(args[9]);
            trainFoldBuilder = new SimpleKFoldBuilder(k, idFile);
            // throws NullPointerException when train=true
            refExtractTrainer = null;
            referenceLineAnnotator = new ParsCitReferenceLineAnnotator(citeExtractFile);
            break;
        }

        if (train) {
            if (!foldTargetDirectory.exists()) {
                foldTargetDirectory.mkdirs();
            }
            File trainingArgsFile = new File(foldTargetDirectory + File.separator + "training-arguments.txt");
            PrintWriter trainingArgsWriter = new PrintWriter(trainingArgsFile);
            int i = 0;
            for (String arg : args) {
                trainingArgsWriter.println(i + ": " + arg);
                i++;
            }

            trainingArgsWriter.println();
            trainingArgsWriter.println(String.join(" ", args));
            trainingArgsWriter.close();
        }
        String evaluationName = "";
        switch (evaluationMode) {
        case 0:
            evaluationName = "line";
            break;
        case 1:
            evaluationName = "ref";
            break;
        }

        List<File> filesToEvaluate = new ArrayList<File>();

        File tmpFoldDir = new File("/tmp/eval-folds_" + dateFormat.format(currentDate));
        for (int i = 0; i < k; i++) {
            File currentFoldDir = new File(foldTargetDirectory + File.separator + i);
            File currentFoldTrainingTargetDir = new File(currentFoldDir + File.separator + "models");

            if (!currentFoldTrainingTargetDir.exists()) {
                currentFoldTrainingTargetDir.mkdirs();
            }

            if (train) {
                File tmpTrainFoldDir = new File(tmpFoldDir + "/train/" + i);
                if (!tmpTrainFoldDir.exists()) {
                    tmpTrainFoldDir.mkdirs();
                    // build fold in tmp dir
                    trainFoldBuilder.build(i, trainingSourceDirectory, tmpTrainFoldDir);
                }
                refExtractTrainer.train(tmpTrainFoldDir, currentFoldTrainingTargetDir);
            }
            referenceLineAnnotator.initializeModels(currentFoldTrainingTargetDir);

            System.out.println(foldTargetDirectory);
            File currentFoldEvaluationTargetDir = new File(
                    currentFoldDir + File.separator + evaluationName + "-evaluations");

            List<File> testFiles = testKFoldDataset.getTestingFold(i);
            for (File testFile : testFiles) {
                System.out.println(testFile);
                List<String> predictedReferenceLines = referenceLineAnnotator.annotateReferenceLinesFromPDF(testFile);
                File annotatedFile = new File(annotatedFilesDirectory + File.separator
                        + FilenameUtils.removeExtension(testFile.getName()) + ".csv");
                List<String> annotatedReferenceLines = Arrays.asList(FileUtils.readFile(annotatedFile).split("\\n"));

                EvaluationResult evaluationResult = new EvaluationResult();
                switch (evaluationMode) {
                case 0:
                    evaluationResult = referenceEvaluator.evaluateReferenceLines(annotatedReferenceLines,
                            predictedReferenceLines);
                    break;
                case 1:
                    evaluationResult = referenceEvaluator.evaluateMergedReferenceStrings(annotatedReferenceLines,
                            predictedReferenceLines);
                    break;
                }
                File currentEvaluationFile = new File(currentFoldEvaluationTargetDir + File.separator
                        + FilenameUtils.removeExtension(testFile.getName()) + ".json");
                EvaluationResult.writeAsJson(evaluationResult, currentEvaluationFile);
                // System.out.println(evaluationResult);
            }
            filesToEvaluate.addAll(Arrays.asList(currentFoldEvaluationTargetDir.listFiles()));
        }

        // run EvaluationResultcalculator
        EvaluationResultCalculator evaluationResultCalculator = new EvaluationResultCalculator();
        evaluationResultCalculator.calculate(filesToEvaluate, foldTargetDirectory, evaluationName + "-results.tsv",
                null);
        org.apache.commons.io.FileUtils.deleteDirectory(tmpFoldDir);
    }

}

package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import de.exciteproject.pdf_evaluation.refextract.CermineReferenceLineAnnotator;
import de.exciteproject.pdf_evaluation.refextract.GrobidDefaultReferenceLineAnnotator;
import de.exciteproject.pdf_evaluation.refextract.GrobidReferenceLineAnnotator;
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

        KFoldBuilder trainFoldBuilder = null;

        KFoldDataset testKFoldDataset = new KFoldDataset(k);
        testKFoldDataset.build(idFile, pdfDirectory);
        RefExtractTrainer refExtractTrainer = null;
        ReferenceLineAnnotator referenceLineAnnotator = null;
        ReferenceEvaluator referenceEvaluator = new ReferenceEvaluator();
        switch (mode) {
        case 1:
            File grobidHomeDirectory = new File(args[8]);
            trainFoldBuilder = new GrobidTrainKFoldBuilder(k, idFile);
            refExtractTrainer = new GrobidRefExtractTrainer(grobidHomeDirectory);
            referenceLineAnnotator = new GrobidReferenceLineAnnotator(grobidHomeDirectory);
            break;
        case 2:
            grobidHomeDirectory = new File(args[8]);
            File defaultModelDirectory = new File(args[9]);
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
            List<String> features = Arrays.asList(args[8].split(","));
            trainFoldBuilder = new SimpleKFoldBuilder(k, idFile);
            refExtractTrainer = new RefextRefExtractTrainer(features);
            referenceLineAnnotator = new RefextReferenceLineAnnotator();
            break;

        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-MM-ss-SSS");
        File tmpFoldDir = new File("/tmp/eval-folds_" + dateFormat.format(new Date()));
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

            // referenceExtractor.extractAnnotatedReferenceLinesFromPDF(pdfFile);

            System.out.println(foldTargetDirectory);
            File currentFoldEvaluationTargetDir = new File(currentFoldDir + File.separator + "evaluations");
            // File evaluationOutputDir=new File()
            List<File> testFiles = testKFoldDataset.getTestingFold(i);
            for (File testFile : testFiles) {
                List<String> predictedReferenceLines = referenceLineAnnotator.annotateReferenceLinesFromPDF(testFile);
                File annotatedFile = new File(annotatedFilesDirectory + File.separator
                        + FilenameUtils.removeExtension(testFile.getName()) + ".csv");
                List<String> annotatedReferenceLines = Arrays.asList(FileUtils.readFile(annotatedFile).split("\\n"));

                EvaluationResult evaluationResult = referenceEvaluator.evaluateReferenceLines(annotatedReferenceLines,
                        predictedReferenceLines);
                System.out.println(evaluationResult);
                File currentEvaluationFile = new File(currentFoldEvaluationTargetDir + File.separator
                        + FilenameUtils.removeExtension(testFile.getName()) + ".json");
                EvaluationResult.writeAsJson(evaluationResult, currentEvaluationFile);
                // System.out.println(evaluationResult);
            }
        }
    }

}

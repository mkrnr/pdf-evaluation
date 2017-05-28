package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.exciteproject.refext.util.FileUtils;

public class EvaluationResultCalculator {

    public static void main(String[] args) throws IOException {
        File evaluationDirectory = new File(args[0]);
        File evaluationResultFile = new File(args[1]);

        EvaluationResultCalculator evaluationResultCalculator = new EvaluationResultCalculator();
        evaluationResultCalculator.calculate(FileUtils.listFilesRecursively(evaluationDirectory), evaluationResultFile,
                0, 0);

    }

    public void calculate(EvaluationResult evaluationResult, File evaluationResultFile) throws IOException {
        List<String> outputLines = new ArrayList<String>();
        outputLines.add("precision:\t" + evaluationResult.getPrecision());
        outputLines.add("recall:\t" + evaluationResult.getRecall());
        outputLines.add("f1 score:\t" + evaluationResult.getF1Score());
        outputLines.add("truePositives:\t" + evaluationResult.truePositives.size());
        outputLines.add("falsePositives:\t" + evaluationResult.falsePositives.size());
        outputLines.add("falseNegatives:\t" + evaluationResult.falseNegatives.size());

        BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(evaluationResultFile));
        for (String outputLine : outputLines) {
            System.out.println(outputLine);
            outputFileWriter.write(outputLine);
            outputFileWriter.newLine();
        }
        outputFileWriter.close();
    }

    public void calculate(List<File> evaluationFiles, File evaluationResultFile, int additionalFalsePositives,
            int additionalFalseNegatives) throws IOException {
        EvaluationResult aggregatedEvaluationResult = new EvaluationResult();

        for (File evaluationDirectoryFile : evaluationFiles) {
            if (evaluationDirectoryFile.getName().endsWith(".json")) {
                aggregatedEvaluationResult.addEvaluationResult(EvaluationResult.readFromJson(evaluationDirectoryFile));
            }
        }

        List<String> additionalFalsePositivesList = new ArrayList<String>();
        for (int i = 0; i < additionalFalsePositives; i++) {
            additionalFalsePositivesList.add("Dummy\tDummy");
        }
        List<String> additionalFalseNegativesList = new ArrayList<String>();
        for (int i = 0; i < additionalFalseNegatives; i++) {
            additionalFalseNegativesList.add("Dummy\tDummy");

        }
        aggregatedEvaluationResult.falsePositives.addAll(additionalFalsePositivesList);
        aggregatedEvaluationResult.falseNegatives.addAll(additionalFalseNegativesList);
        this.calculate(aggregatedEvaluationResult, evaluationResultFile);
    }

}

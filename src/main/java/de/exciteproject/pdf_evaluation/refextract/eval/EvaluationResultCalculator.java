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
        evaluationResultCalculator.calculate(evaluationDirectory, evaluationResultFile);
    }

    public void calculate(File evaluationDirectory, File evaluationResultFile) throws IOException {
        List<File> evaluationDirectoryFiles = FileUtils.listFilesRecursively(evaluationDirectory);
        EvaluationResult aggregatedEvaluationResult = new EvaluationResult();

        for (File evaluationDirectoryFile : evaluationDirectoryFiles) {
            if (evaluationDirectoryFile.getName().endsWith(".json")) {
                aggregatedEvaluationResult.addEvaluationResult(EvaluationResult.readFromJson(evaluationDirectoryFile));
            }
        }
        List<String> outputLines = new ArrayList<String>();
        outputLines.add("precision:\t" + aggregatedEvaluationResult.getPrecision());
        outputLines.add("recall:\t" + aggregatedEvaluationResult.getRecall());
        outputLines.add("f1 score:\t" + aggregatedEvaluationResult.getF1Score());

        BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(evaluationResultFile));
        for (String outputLine : outputLines) {
            System.out.println(outputLine);
            outputFileWriter.write(outputLine);
            outputFileWriter.newLine();
        }
        outputFileWriter.close();
    }
}

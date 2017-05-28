package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EvaluationResultCalculator {

    public static void main(String[] args) throws IOException {
        File foldsDirectory = new File(args[0]);
        File evaluationResultFile = new File(args[1]);
        String evaluationsDirectoryName = args[2];
        int additionalFalseNegatives = Integer.parseInt(args[3]);
        int additionalFalsePositives = Integer.parseInt(args[4]);
        String filterRegex = null;
        if (args.length > 5) {
            filterRegex = args[5];
        }

        List<File> evaluationFiles = new ArrayList<File>();
        for (File foldDirectory : foldsDirectory.listFiles()) {
            if (foldDirectory.isDirectory()) {
                for (File foldFile : foldDirectory.listFiles()) {
                    if (foldFile.getName().equals(evaluationsDirectoryName)) {
                        evaluationFiles.addAll(Arrays.asList(foldFile.listFiles()));
                    }
                }
            }
        }

        EvaluationResultCalculator evaluationResultCalculator = new EvaluationResultCalculator();
        evaluationResultCalculator.calculate(evaluationFiles, evaluationResultFile, filterRegex,
                additionalFalseNegatives, additionalFalsePositives);

    }

    public void calculate(EvaluationResult evaluationResult, File evaluationResultFile, String filterRegex)
            throws IOException {

        if (filterRegex != null) {
            List<List<String>> evaluationResultLists = new ArrayList<List<String>>();
            evaluationResultLists.add(evaluationResult.truePositives);
            evaluationResultLists.add(evaluationResult.falseNegatives);
            evaluationResultLists.add(evaluationResult.falsePositives);
            for (List<String> evaluationResultList : evaluationResultLists) {
                List<Integer> indicesToRemove = new ArrayList<Integer>();
                for (int i = 0; i < evaluationResultList.size(); i++) {
                    if (!evaluationResultList.get(i).matches(filterRegex)) {
                        indicesToRemove.add(i);
                    }
                }

                for (int i = indicesToRemove.size() - 1; i >= 0; i--) {
                    evaluationResultList.remove((int) indicesToRemove.get(i));
                }
            }
        }

        List<String> outputLines = new ArrayList<String>();

        outputLines.add("filterRegex:\t" + filterRegex);
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

    public void calculate(List<File> evaluationFiles, File evaluationResultFile, String filterRegex,
            int additionalFalseNegatives, int additionalFalsePositives) throws IOException {
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

        this.calculate(aggregatedEvaluationResult, evaluationResultFile, filterRegex);
    }

}

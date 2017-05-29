package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

public class EvaluationResultCalculator {

    public static void main(String[] args) throws IOException {
        File foldsDirectory = new File(args[0]);
        File evaluationResultFile = new File(args[1]);
        String evaluationsDirectoryName = args[2];
        // TODO parse map as input: filename;count,filename;count
        String additionalFalseNegatives = args[3];
        String filterRegex = null;
        if (args.length > 4) {
            filterRegex = args[4];
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
                additionalFalseNegatives);

    }

    public void calculate(List<File> evaluationFiles, File evaluationResultFile, String filterRegex,
            String additionalFalseNegatives) throws IOException {

        List<EvaluationResult> evaluationResults = new ArrayList<EvaluationResult>();

        String[] keyValuePairsSplit = new String[0];
        if (additionalFalseNegatives.contains(",")) {
            keyValuePairsSplit = additionalFalseNegatives.split(",");
        }
        Map<String, Integer> valuesToAddMap = new HashMap<String, Integer>();
        for (String keyValuePair : keyValuePairsSplit) {
            String[] keyValuePairSplit = keyValuePair.split(";");
            valuesToAddMap.put(keyValuePairSplit[0], Integer.parseInt(keyValuePairSplit[1]));
        }

        for (File evaluationFile : evaluationFiles) {
            if (evaluationFile.getName().endsWith(".json")) {

                EvaluationResult evaluationResult = EvaluationResult.readFromJson(evaluationFile);
                System.out.println();
                System.out.println(evaluationResult.truePositives.size());
                this.filterEvaluationResult(evaluationResult, filterRegex);
                System.out.println(evaluationResult.truePositives.size());
                System.out.println();

                String evaluationFileNameWithoutEnding = FilenameUtils.removeExtension(evaluationFile.getName());
                if (valuesToAddMap.containsKey(evaluationFileNameWithoutEnding)) {
                    System.out.println(valuesToAddMap.get(evaluationFileNameWithoutEnding));
                    for (int i = 0; i < valuesToAddMap.get(evaluationFileNameWithoutEnding); i++) {
                        evaluationResult.falseNegatives.add("Dummy");
                    }
                }
                evaluationResults.add(evaluationResult);
            }
        }

        // calculate micro metrics
        EvaluationResult aggregatedEvaluationResult = new EvaluationResult();

        for (EvaluationResult evaluationResult : evaluationResults) {
            aggregatedEvaluationResult.addEvaluationResult(evaluationResult);
        }

        // calculate macro metrics
        double macroPrecision = 0.0;
        double macroRecall = 0.0;
        double macroF1Score = 0.0;
        for (EvaluationResult evaluationResult : evaluationResults) {
            macroPrecision += evaluationResult.getPrecision();
            macroRecall += evaluationResult.getRecall();
            macroF1Score += evaluationResult.getF1Score();
        }
        macroPrecision = macroPrecision / evaluationResults.size();
        macroRecall = macroRecall / evaluationResults.size();
        macroF1Score = macroF1Score / evaluationResults.size();

        List<String> outputLines = new ArrayList<String>();

        outputLines.add("Name\tValue");
        outputLines.add("filterRegex\t" + filterRegex);
        if (keyValuePairsSplit.length > 0) {
            outputLines.add("addedFalseNegatives\t" + additionalFalseNegatives);
        }
        outputLines.add("micro precision\t" + aggregatedEvaluationResult.getPrecision());
        outputLines.add("micro recall\t" + aggregatedEvaluationResult.getRecall());
        outputLines.add("micro f1 score\t" + aggregatedEvaluationResult.getF1Score());

        outputLines.add("macro precision\t" + macroPrecision);
        outputLines.add("macro recall\t" + macroRecall);
        outputLines.add("macro f1 score\t" + macroF1Score);

        outputLines.add("truePositives\t" + aggregatedEvaluationResult.truePositives.size());
        outputLines.add("falseNegatives\t" + aggregatedEvaluationResult.falseNegatives.size());
        outputLines.add("falsePositives\t" + aggregatedEvaluationResult.falsePositives.size());

        BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(evaluationResultFile));
        for (String outputLine : outputLines) {
            System.out.println(outputLine);
            outputFileWriter.write(outputLine);
            outputFileWriter.newLine();
        }
        outputFileWriter.close();
    }

    private void filterEvaluationResult(EvaluationResult evaluationResult, String filterRegex) {
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
    }
}

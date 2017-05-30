package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;

public class EvaluationResultCalculator {

    public static void main(String[] args) throws IOException {
        File foldsDirectory = new File(args[0]);
        String evaluationResultFileName = args[1];
        String evaluationsDirectoryName = args[2];
        String filterRegex = null;
        if (args.length > 3) {
            filterRegex = args[3];
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
        evaluationResultCalculator.calculate(evaluationFiles, foldsDirectory, evaluationResultFileName, filterRegex);

    }

    public void calculate(List<File> evaluationFiles, File evaluationResultDirectory, String evaluationResultFileSuffix,
            String filterRegex) throws IOException {

        List<EvaluationResult> evaluationResults = new ArrayList<EvaluationResult>();

        Map<String, Double> precisionMap = new HashMap<String, Double>();
        Map<String, Double> recallMap = new HashMap<String, Double>();
        Map<String, Double> f1ScoreMap = new HashMap<String, Double>();
        for (File evaluationFile : evaluationFiles) {
            if (evaluationFile.getName().endsWith(".json")) {
                EvaluationResult evaluationResult = EvaluationResult.readFromJson(evaluationFile);
                this.filterEvaluationResult(evaluationResult, filterRegex);

                String evaluationResultName = FilenameUtils.removeExtension(evaluationFile.getName());
                precisionMap.put(evaluationResultName, evaluationResult.getPrecision());
                recallMap.put(evaluationResultName, evaluationResult.getRecall());
                f1ScoreMap.put(evaluationResultName, evaluationResult.getF1Score());

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
        outputLines.add("micro precision\t" + aggregatedEvaluationResult.getPrecision());
        outputLines.add("micro recall\t" + aggregatedEvaluationResult.getRecall());
        outputLines.add("micro f1 score\t" + aggregatedEvaluationResult.getF1Score());

        outputLines.add("macro precision\t" + macroPrecision);
        outputLines.add("macro recall\t" + macroRecall);
        outputLines.add("macro f1 score\t" + macroF1Score);

        outputLines.add("truePositives\t" + aggregatedEvaluationResult.truePositives.size());
        outputLines.add("falseNegatives\t" + aggregatedEvaluationResult.falseNegatives.size());
        outputLines.add("falsePositives\t" + aggregatedEvaluationResult.falsePositives.size());
        for (String outputLine : outputLines) {
            System.out.println(outputLine);
        }

        File statisticsOutputFile = new File(evaluationResultDirectory + File.separator + evaluationResultFileSuffix);

        this.writeToFile(outputLines, statisticsOutputFile);

        this.writeToFile(this.sortedSetToList(this.entriesSortedByValues(precisionMap)),
                new File(evaluationResultDirectory + File.separator + "precision-" + evaluationResultFileSuffix));
        this.writeToFile(this.sortedSetToList(this.entriesSortedByValues(recallMap)),
                new File(evaluationResultDirectory + File.separator + "recall-" + evaluationResultFileSuffix));
        this.writeToFile(this.sortedSetToList(this.entriesSortedByValues(f1ScoreMap)),
                new File(evaluationResultDirectory + File.separator + "f1Score-" + evaluationResultFileSuffix));

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

    private List<String> sortedSetToList(SortedSet<Entry<String, Double>> sortedSet) {
        List<String> list = new ArrayList<String>();
        for (Entry<String, Double> entry : sortedSet) {
            list.add(entry.getKey() + "\t" + entry.getValue());
        }
        return list;

    }

    private void writeToFile(List<String> list, File outputFile) throws IOException {
        BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(outputFile));
        for (String outputLine : list) {
            outputFileWriter.write(outputLine);
            outputFileWriter.newLine();
        }
        outputFileWriter.close();

    }

    // from: https://stackoverflow.com/a/2864923/2174538
    <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                int res = e1.getValue().compareTo(e2.getValue());
                return res != 0 ? res : 1;
            }
        });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
}

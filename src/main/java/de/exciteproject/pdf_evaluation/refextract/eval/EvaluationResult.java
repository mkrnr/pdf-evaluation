package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.exciteproject.pdf_evaluation.util.JsonUtils;

public class EvaluationResult {
    private List<String> truePositives;
    private List<String> falseNegatives;
    private List<String> falsePositives;

    public EvaluationResult(List<String> matchedLines, List<String> tempCorrectLines, List<String> tempPredictedLines) {
        this.truePositives = matchedLines;
        this.falseNegatives = tempCorrectLines;
        this.falsePositives = tempPredictedLines;
    }

    public EvaluationResult() {
        this.truePositives = new ArrayList<String>();
        this.falseNegatives = new ArrayList<String>();
        this.falsePositives = new ArrayList<String>();
    }

    public double getF1Score() {
        double precision = this.getPrecision();
        double recall = this.getRecall();
        return 2 * (precision * recall) / (precision + recall);
    }

    public void addEvaluationResult(EvaluationResult evaluationResult) {
        this.truePositives.addAll(evaluationResult.truePositives);
        this.falseNegatives.addAll(evaluationResult.falseNegatives);
        this.falsePositives.addAll(evaluationResult.falsePositives);
    }

    public double getPrecision() {

        return (double) truePositives.size() / (truePositives.size() + falsePositives.size());
    }

    public double getRecall() {
        return (double) truePositives.size() / (truePositives.size() + falseNegatives.size());
    }

    public static EvaluationResult readFromJson(File inputFile) {
        return (EvaluationResult) JsonUtils.readFromFile(EvaluationResult.class, inputFile);
    }

    public static void writeAsJson(EvaluationResult evaluationResult, File outputFile) {
        JsonUtils.writeToFile(evaluationResult, outputFile);
    }

    public String toString() {
        String evaluationResult = "";

        evaluationResult += "precision: " + this.getPrecision() + "\n";
        evaluationResult += "recall: " + this.getRecall() + "\n";
        evaluationResult += "F1 score: " + this.getF1Score() + "\n";
        evaluationResult += "falseNegatives: " + falseNegatives.size() + "\n";
        for (String falseNegative : falseNegatives) {
            evaluationResult += ("\t" + falseNegative + "\n");
        }
        evaluationResult += "falsePositives: " + falsePositives.size() + "\n";
        for (String falsePositive : falsePositives) {
            evaluationResult += ("\t" + falsePositive + "\n");
        }
        evaluationResult += "truePositives: " + truePositives.size() + "\n";
        for (String truePositive : truePositives) {
            evaluationResult += ("\t" + truePositive + "\n");
        }
        return evaluationResult;
    }

}

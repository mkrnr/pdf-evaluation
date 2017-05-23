package de.exciteproject.pdf_evaluation.refextract.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.exciteproject.refext.util.FileUtils;

public class ReferenceEvaluator {

    public static void main(String[] args) throws IOException {
        File correctDir = new File(args[0]);
        File predictedDir = new File(args[1]);
        File outputDir = new File(args[2]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        // load configuration file specifying the retrained SVM models for zone
        // classification

        ReferenceEvaluator referenceEvaluator = new ReferenceEvaluator();
        for (File predictedFile : predictedDir.listFiles()) {
            File correctFile = new File(correctDir.getAbsolutePath() + File.separator + predictedFile.getName());
            File outputFile = new File(outputDir.getAbsolutePath() + File.separator + correctFile.getName());
            System.out.println("check: " + correctFile.getName());
            EvaluationResult evaluationResult = referenceEvaluator.evaluateMergedReferenceStrings(correctFile,
                    predictedFile);
            // EvaluationResult evaluationResult =
            // referenceEvaluator.evaluateReferenceLines(correctFile,
            // predictedFile);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            bufferedWriter.write(evaluationResult.toString());

            bufferedWriter.close();

        }
    }

    public EvaluationResult evaluateReferenceLines(File correctFile, File predictedFile) throws IOException {
        List<String> correctLines = this.readLines(correctFile);
        List<String> predictedFileContent = this.readLines(predictedFile);
        return this.compareStrings(correctLines, predictedFileContent);
    }

    public EvaluationResult evaluateReferenceLines(List<String> correctLines, List<String> predictedLines)
            throws IOException {
        return this.compareStrings(correctLines, predictedLines);
    }

    public EvaluationResult evaluateMergedReferenceStrings(File correctFile, File predictedFile) throws IOException {
        List<String> correctLines = this.readLines(correctFile);
        List<String> predictedLines = this.readLines(predictedFile);

        List<String> mergedCorrectLines = this.mergeLinesToReferences(correctLines);
        List<String> mergedPredictedLines = this.mergeLinesToReferences(predictedLines);
        return this.compareStrings(mergedCorrectLines, mergedPredictedLines);
    }

    public EvaluationResult evaluateMergedReferenceStrings(List<String> correctLines, List<String> predictedLines)
            throws IOException {

        List<String> mergedCorrectLines = this.mergeLinesToReferences(correctLines);
        List<String> mergedPredictedLines = this.mergeLinesToReferences(predictedLines);
        return this.compareStrings(mergedCorrectLines, mergedPredictedLines);
    }

    /**
     * During the merging, lines are simply concatenated. This simple approach
     * is intended for evaluation purposes only
     * 
     * @param referenceLines
     * @return
     */
    private List<String> mergeLinesToReferences(List<String> referenceLines) {
        List<String> references = new ArrayList<String>();
        String reference = "";
        for (String referenceLine : referenceLines) {
            if (referenceLine.startsWith("B-REF\t")) {
                if (!reference.isEmpty()) {
                    references.add(reference);
                }
                reference = referenceLine.replaceFirst("B-REF\t", "");
            }
            if (referenceLine.startsWith("I-REF\t")) {
                reference += referenceLine.replaceFirst("I-REF\t", "");
            }
        }
        return references;
    }

    private List<String> readLines(File file) throws IOException {
        return Arrays.asList(FileUtils.readFile(file).split("\\n"));
    }

    private EvaluationResult compareStrings(List<String> correctLines, List<String> predictedLines) {

        List<String> tempCorrectLines = new ArrayList<String>(correctLines);
        List<String> tempPredictedLines = new ArrayList<String>(predictedLines);

        List<String> matchedLines = new ArrayList<String>();

        for (int i = tempPredictedLines.size() - 1; i >= 0; i--) {
            for (int j = tempCorrectLines.size() - 1; j >= 0; j--) {
                if (tempCorrectLines.get(j).equals(tempPredictedLines.get(i))) {
                    matchedLines.add(0, tempPredictedLines.get(i));
                    tempCorrectLines.remove(j);
                    tempPredictedLines.remove(i);
                    break;
                }
            }
        }
        return new EvaluationResult(matchedLines, tempCorrectLines, tempPredictedLines);
    }

}

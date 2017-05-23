package de.exciteproject.pdf_evaluation.refextract;

import java.io.File;
import java.util.List;

import pl.edu.icm.cermine.ComponentConfiguration;
import pl.edu.icm.cermine.ExtractionUtils;
import pl.edu.icm.cermine.exception.AnalysisException;
import pl.edu.icm.cermine.structure.model.BxDocument;
import pl.edu.icm.cermine.tools.BxDocUtils;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

public class CermineTruVizReferenceExtractor {

    public static void main(String[] args) throws AnalysisException {
        File inputFile = new File(args[0]);

        ComponentConfiguration componentConfig = new ComponentConfiguration();

        try {
            BxDocument document = BxDocUtils.getDocument(inputFile);

            List<String> results = ExtractionUtils.extractRefStrings(componentConfig, document);
            for (String result : results) {
                System.out.println(result);
            }
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AnalysisException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO figure out why
            // InlineImageParseException/InvocationTargetException is not caught
            e.printStackTrace();
        }
    }

}

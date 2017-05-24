package de.exciteproject.pdf_evaluation.stats;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import de.exciteproject.refext.util.FileUtils;

/**
 * Class for generating language statistics for a directory containing text
 * files of different languages.
 */
public class LanguageAnalyzer {

    /**
     * First argument: Directory containing files to be analyzed (can contain
     * subfolders)
     * <p>
     * Second argument: File in which the results will be written
     *
     * @param args
     * @throws IOException
     * @throws LangDetectException
     */
    public static void main(String[] args) throws IOException, LangDetectException {
        File inputDirectory = new File(args[0]);
        File outputFile = new File(args[1]);

        LanguageAnalyzer languageAnalyzer = new LanguageAnalyzer();
        languageAnalyzer.analyze(inputDirectory, outputFile);
    }

    public LanguageAnalyzer() throws LangDetectException, IOException {
        // solution for loading detector profiles from jar taken from:
        // http://stackoverflow.com/a/15332031

        String dirname = "profiles/";
        Enumeration<URL> en = Detector.class.getClassLoader().getResources(dirname);
        List<String> profiles = new ArrayList<>();
        if (en.hasMoreElements()) {
            URL url = en.nextElement();
            JarURLConnection urlcon = (JarURLConnection) url.openConnection();
            try (JarFile jar = urlcon.getJarFile();) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement().getName();
                    if (entry.startsWith(dirname)) {
                        try (InputStream in = Detector.class.getClassLoader().getResourceAsStream(entry);) {
                            profiles.add(IOUtils.toString(in));
                        }
                    }
                }
            }
        }
        DetectorFactory.loadProfile(profiles);
    }

    /**
     *
     * @param inputDirectory:
     *            directory containing text files to be analyzed
     * @param outputFile:
     *            file in which the analysis results are written
     * @throws IOException
     * @throws LangDetectException
     */
    public void analyze(File inputDirectory, File outputFile) throws IOException, LangDetectException {
        // List<File> textFiles =
        // FileUtils.listFilesRecursively(inputDirectory);

        // Map<String, Integer> languageMap = new HashMap<String, Integer>();

        for (File inputFile : inputDirectory.listFiles()) {
            Detector detector = DetectorFactory.create();
            detector.append(FileUtils.readFile(inputFile));
            try {
                // MapUtils.addCount(languageMap, detector.detect());
                // System.out.println(inputFile);
                System.out.println(detector.detect());
            } catch (LangDetectException e) {
                // MapUtils.addCount(languageMap, "unknown");
                System.out.println(inputFile);
                System.out.println("unknown");
            }

        }

        // BufferedWriter bufferedWriter = new BufferedWriter(new
        // FileWriter(outputFile));
        // bufferedWriter.write("number of files files: " + textFiles.size());
        // bufferedWriter.newLine();
        // bufferedWriter.newLine();
        //
        // for (Entry<String, Integer> languageMapEntry :
        // MapUtils.entriesReverselySortedByValues(languageMap)) {
        // bufferedWriter.write(languageMapEntry.getKey() + ": " +
        // languageMapEntry.getValue());
        // bufferedWriter.newLine();
        // }
        // bufferedWriter.close();

    }

}

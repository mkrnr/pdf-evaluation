package de.exciteproject.pdf_evaluation.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils {

    public static Object readFromFile(Class<?> objectClass, File inputFile) {
        String jsonString = null;
        try {
            jsonString = FileUtils.readFileToString(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Gson().fromJson(jsonString, objectClass);
    }

    public static Object readFromFile(Type objectType, File inputFile) {
        String jsonString = null;
        try {
            jsonString = FileUtils.readFileToString(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Gson().fromJson(jsonString, objectType);
    }

    public static void writeToFile(Object object, File outputFile) {

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileUtils.writeStringToFile(outputFile, gson.toJson(object));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

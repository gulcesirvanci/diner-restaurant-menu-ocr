package com.example.ocrGUI.models;

import zemberek.classification.FastTextClassifier;
import zemberek.core.ScoredItem;
import zemberek.core.turkish.Turkish;
import zemberek.tokenization.TurkishTokenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Classifier {

    public String classify(String s) {
        Path path = Paths.get("./src/main/resources/raw.model.q");
        FastTextClassifier classifier = null;
        try {
            classifier = FastTextClassifier.load(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String processed = String.join(" ", TurkishTokenizer.DEFAULT.tokenizeToStrings(s));
        processed = processed.toLowerCase(Turkish.LOCALE);

        if(processed.matches("\\d+(,\\d{1,2})?(.*)?"))
            return "price";

        List<ScoredItem<String>> res = classifier.predict(processed, 1);

        return findLabel(processed, res);
    }

    private String findLabel(String data, List<ScoredItem<String>> scores){
        if(scores.isEmpty())
            return "name"; //If not classified, its label is name by default}

        if(scores.get(0).toString().startsWith("__label__description"))
            return "description";

        if(scores.get(0).toString().startsWith("__label__name"))
            return "name";

        return "category";
    }
}


package com.metaversant.alfresco.nlp.enricher.service;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;

/**
 * Created by jpotts, Metaversant on 2019-01-13.
 */
public class BaseExtractor {
    private Tokenizer tokenizer = SimpleTokenizer.INSTANCE;

    public String[] extract(NameFinderME finder, String sentence) {
        if (finder != null) {
            // Split the sentence into tokens
            String[] tokens = tokenizer.tokenize(sentence);

            // Find the names in the tokens and return Span objects
            Span[] nameSpans = finder.find(tokens);

            return Span.spansToStrings(nameSpans, tokens);
        } else {
            return null;
        }
    }

}

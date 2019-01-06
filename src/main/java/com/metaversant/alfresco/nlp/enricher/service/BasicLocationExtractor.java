package com.metaversant.alfresco.nlp.enricher.service;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Created by jpotts, Metaversant on 7/2/17.
 */
@Service
public class BasicLocationExtractor {
    private static final Logger logger = LogManager.getLogger(BasicLocationExtractor.class);

    private Tokenizer tokenizer = SimpleTokenizer.INSTANCE;
    private TokenNameFinderModel model;
    private NameFinderME finder;

    public BasicLocationExtractor() {
        try {
            // Load the model file downloaded from OpenNLP
            // http://opennlp.sourceforge.net/models-1.5/en-ner-person.bin
            model = new TokenNameFinderModel(this.getClass().getClassLoader().getResource("en-ner-location.bin"));
        } catch (IOException ioe) {
            logger.error(ioe.getMessage());
        }

        // Create a NameFinder using the model
        if (model != null) {
            finder = new NameFinderME(model);
        }
    }

    public String[] extract(String sentence) {
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

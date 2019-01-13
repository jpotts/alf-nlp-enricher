package com.metaversant.alfresco.nlp.enricher.service;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by jpotts, Metaversant on 7/2/17.
 */
@Service
public class BasicNameExtractor extends BaseExtractor {
    private static final Logger logger = LogManager.getLogger(BasicNameExtractor.class);

    private TokenNameFinderModel model;
    private NameFinderME finder;

    public BasicNameExtractor() {
        try {
            // Load the model file downloaded from OpenNLP
            // http://opennlp.sourceforge.net/models-1.5/en-ner-person.bin
            model = new TokenNameFinderModel(this.getClass().getClassLoader().getResource("en-ner-person.bin"));
        } catch (IOException ioe) {
            logger.error(ioe.getMessage());
        }

        if (model != null) {
            // Create a NameFinder using the model
            finder = new NameFinderME(model);
        }
    }

    public String[] extract(String sentence) {
        return extract(finder, sentence);
    }
}

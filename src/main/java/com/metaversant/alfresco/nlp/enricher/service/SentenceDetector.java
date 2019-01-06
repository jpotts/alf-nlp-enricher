package com.metaversant.alfresco.nlp.enricher.service;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Created by jpotts, Metaversant on 10/1/18.
 */
@Service
public class SentenceDetector {
    private static final Logger logger = LogManager.getLogger(SentenceDetector.class);

    private SentenceModel model;
    private SentenceDetectorME detector;

    public SentenceDetector() {
        //Loading sentence detector model
        SentenceModel model = null;
        try {
            model = new SentenceModel(this.getClass().getClassLoader().getResource("en-sent.bin"));
        } catch (IOException ioe) {
            logger.error(ioe.getMessage());
        }

        if (model != null) {
            //Instantiating the SentenceDetectorME class
            detector = new SentenceDetectorME(model);
        }
    }

    public String[] detect(String text) {
        if (detector != null) {
            return detector.sentDetect(text);
        } else {
            return null;
        }
    }
}

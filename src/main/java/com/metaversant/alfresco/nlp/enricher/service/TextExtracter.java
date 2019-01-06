package com.metaversant.alfresco.nlp.enricher.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.*;

/**
 * Created by jpotts, Metaversant on 10/1/18.
 */
@Service
public class TextExtracter {
    private static final Logger logger = LogManager.getLogger(TextExtracter.class);

    public static void main(String[] args) {
        TextExtracter et = new TextExtracter();
        try {
            et.parseToPlainText(args[0], args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseToPlainText(String inFilePath, String outFilePath) throws IOException, SAXException, TikaException {
        BodyContentHandler handler = new BodyContentHandler();

        TikaConfig config = new TikaConfig(this.getClass().getClassLoader().getResource("tika-config.xml"));
        AutoDetectParser parser = new AutoDetectParser(config);

        Metadata metadata = new Metadata();
        String text = null;
        InputStream stream = null;
        try {
            File file = new File(inFilePath);
            stream = new FileInputStream(file);
            parser.parse(stream, handler, metadata);
            text = handler.toString();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        PrintWriter pw = new PrintWriter(outFilePath);
        pw.print(text);
        pw.close();
        logger.debug("Text file written to: " + outFilePath);
    }

}

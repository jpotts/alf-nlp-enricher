package com.metaversant.alfresco.nlp.enricher.service;

import com.metaversant.alfresco.nlp.enricher.exceptions.AlfrescoServiceException;
import com.metaversant.alfresco.nlp.enricher.model.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by jpotts, Metaversant on 10/11/18.
 */
@Service
public class NodeProcessor {
    private static final Logger logger = LogManager.getLogger(NodeProcessor.class);
    public static final String PROP_LOCATIONS = "mge:locationEntities";
    public static final String PROP_ORGS = "mge:orgEntities";
    public static final String PROP_NAMES = "mge:nameEntities";

    @Value("${work.path}")
    private String workPath;

    @Autowired
    private AlfrescoService alfrescoService;

    @Autowired
    private Downloader downloader;

    @Autowired
    private TextExtracter textExtracter;

    @Autowired
    private BasicLocationExtractor locationExtractor;

    @Autowired
    private BasicNameExtractor nameExtractor;

    @Autowired
    private BasicOrgExtractor orgExtractor;

    @Autowired
    private SentenceDetector sentenceDetector;

    // For POC, keeping a local list of files we've seen. In an actual app, we'd want to write
    // this to a DB or Redis or something similar in case we scale up the number of listeners
    private HashMap<String, String> pastHashesById = new HashMap<>();

    public void process(Node node) {
        logger.debug("Inside NodeProcessor.process");

        String id = node.getId();
        String downloadFilePath = getDownloadFilePath(id);
        String textFilePath = getTextFilePath(id);

        // Download content
        downloader.download(id, downloadFilePath);

        // Create a hash and add it to a list
        String hash = null;
        try {
            hash = HashSumGenerator.getHash(new FileInputStream(new File(downloadFilePath)));
            logger.debug("Hash: " + hash);
        } catch (FileNotFoundException fnfe) {
            logger.error("Download file not found");
        }

        // If we have seen this exact content before for this node, stop
        String pastHash = pastHashesById.get(id);
        if (pastHash != null) {
            logger.debug("Past hash: " + pastHash);
            if (pastHash.equals(hash)) {
                logger.debug("Have already processed this exact file for this id, skipping");
                deleteFile(downloadFilePath);
                return;
            }
        }

        // Add hash to list of past hashes
        pastHashesById.put(id, hash);

        // Create txt
        try {
            textExtracter.parseToPlainText(getDownloadFilePath(id), textFilePath);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }

        // Run extractions
        String text = null;
        try {
            text = new String(Files.readAllBytes(Paths.get(textFilePath)));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }

        // Detect the sentences
        String sentences[] = sentenceDetector.detect(text);

        HashSet<String> locations = new HashSet<>();
        HashSet<String> orgs = new HashSet<>();
        HashSet<String> names = new HashSet<>();

        for (String sentence : sentences) {
            locations = addToSet(locationExtractor.extract(sentence), locations);
            orgs = addToSet(orgExtractor.extract(sentence), orgs);
            names = addToSet(nameExtractor.extract(sentence), names);
        }

        logger.debug("Locations: " + locations);
        logger.debug("Orgs: " + orgs);
        logger.debug("Names: " + names);

        // Create a set of properties for the extracted entities
        HashMap<String, Serializable> properties = new HashMap<>();
        properties.put(PROP_LOCATIONS, toArrayList(locations));
        properties.put(PROP_ORGS, toArrayList(orgs));
        properties.put(PROP_NAMES, toArrayList(names));

        // Update the node in Alfresco
        try {
            alfrescoService.updateNode(id, properties);
        } catch (AlfrescoServiceException ase) {
            logger.error(ase.getMessage());
        }

        // Cleanup
        deleteFile(textFilePath);
        deleteFile(downloadFilePath);
    }

    private void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    private ArrayList<String> toArrayList(HashSet<String> set) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (String item : set) {
            arrayList.add(item);
        }
        return arrayList;
    }

    private HashSet<String> addToSet(String[] items, HashSet<String> set) {
        for (int i = 0; i < items.length; i++) {
            if (!set.contains(items[i])) {
                set.add(items[i]);
            }
        }
        return set;
    }

    private String getDownloadFilePath(String id) {
        return workPath + "/" + id + ".bin";
    }

    private String getTextFilePath(String id) {
        return workPath + "/" + id + ".txt";
    }
}

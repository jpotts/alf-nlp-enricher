package com.metaversant.alfresco.nlp.enricher.listeners;

import com.metaversant.kafka.model.NodeEvent;
import com.metaversant.alfresco.nlp.enricher.model.Node;
import com.metaversant.alfresco.nlp.enricher.service.AlfrescoService;
import com.metaversant.alfresco.nlp.enricher.service.NodeProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Created by jpotts, Metaversant on 9/20/18.
 */
@Service
public class KafkaConsumer {
    private static final Logger logger = LogManager.getLogger(KafkaConsumer.class);

    @Autowired
    NodeProcessor nodeProcessor;

    @Autowired
    AlfrescoService alfrescoService;

    @KafkaListener(topics="${kafka.topic}", group = "${kafka.group}", containerFactory = "nodeEventKafkaListenerFactory")
    public void consumeJson(NodeEvent nodeEvent) {
        logger.debug("Inside consumeJson");
        try {
            // If the event is for certain types that we don't care about, bail out
            if (nodeEvent.getContentType().equals("F:cm:systemfolder") ||
                    nodeEvent.getContentType().equals("F:bpm:package") ||
                    nodeEvent.getContentType().equals("I:act:actionparameter") ||
                    nodeEvent.getContentType().equals("I:act:action") ||
                    nodeEvent.getContentType().equals("D:cm:thumbnail") ||
                    nodeEvent.getContentType().equals("cmis:folder")) {
                return;
            }

            // If the event is create or delete, bail
            if (nodeEvent.getEventType().equals(NodeEvent.EventType.DELETE)) {
                logger.debug("Event is a delete, skipping");
                return;
            }

            // If the size of the object is 0, bail
            if (nodeEvent.getSize() == 0) {
                logger.debug("Content stream is 0 length, skipping");
                return;
            }

            // If the event is create, update, or ping, grab the full object from Alfresco
            if (nodeEvent.getEventType().equals(NodeEvent.EventType.CREATE) ||
                    nodeEvent.getEventType().equals(NodeEvent.EventType.UPDATE) ||
                    nodeEvent.getEventType().equals(NodeEvent.EventType.PING)) {

                logger.debug("Event type: " + nodeEvent.getEventType());

                // Fetch the node from Alfresco
                Node node = alfrescoService.getNode(nodeEvent.getNodeRef());

                // Copy some of the properties from the event onto the node object that are not easily accessed
                // from the CMIS object
                if (nodeEvent.getParent() != null) {
                    node.setParent(nodeEvent.getParent());
                }

                if (nodeEvent.getSiteId() != null) {
                    node.setSiteId(nodeEvent.getSiteId());
                }

                if (!hasAspect(node, "P:mge:enrichable")) {
                    logger.debug("Node lacks the necessary aspect, skipping");
                    return;
                }

                // Process the node
                nodeProcessor.process(node);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public boolean hasAspect(Node node, String aspectId) {
        ArrayList<String> aspects = (ArrayList<String>) node.getMetadata().get("secondaryObjectTypeIds");
        if (aspects == null || aspects.size() == 0) {
            logger.debug("Object has no aspects");
            return false;
        }

        for (String aspect : aspects) {
            if (aspect.equals(aspectId)) {
                logger.debug("Found aspect: " + aspectId);
                return true;
            } else {
                logger.debug("Aspect does not match: " + aspect);
            }
        }

        return false;
    }
}

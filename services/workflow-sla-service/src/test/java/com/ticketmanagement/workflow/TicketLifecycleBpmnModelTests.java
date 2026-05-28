package com.ticketmanagement.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class TicketLifecycleBpmnModelTests {

    private static final String BPMN_NS = "http://www.omg.org/spec/BPMN/20100524/MODEL";

    @Test
    void ticketLifecycleProcessDeclaresExpectedSignalsAndStatusPath() throws Exception {
        Document document = loadTicketLifecycleModel();

        Element process = elementById(document, "process", "ticketLifecycle");
        Set<String> signals = signalNames(document);
        Set<String> flows = sequenceFlows(document);

        assertThat(process.getAttribute("isExecutable")).isEqualTo("true");
        assertThat(signals).containsExactlyInAnyOrder(
                "START_PROGRESS",
                "REQUEST_CUSTOMER_INFO",
                "CUSTOMER_RESPONDED",
                "RESOLVE_TICKET",
                "CLOSE_TICKET",
                "REOPEN_TICKET");
        assertThat(flows).contains(
                "ticketCreated->statusNew",
                "statusNew->enterInProgress",
                "enterInProgress->statusInProgress",
                "statusInProgress->requestCustomerInfo",
                "requestCustomerInfo->statusWaitingForCustomer",
                "statusWaitingForCustomer->enterInProgress",
                "statusInProgress->resolveTicket",
                "resolveTicket->statusResolved",
                "statusResolved->closeTicket",
                "closeTicket->statusClosed",
                "statusResolved->reopenTicket",
                "reopenTicket->enterInProgress");
    }

    @Test
    void closedStatusIsTerminalInBpmnModel() throws Exception {
        Document document = loadTicketLifecycleModel();

        Element closed = elementById(document, "endEvent", "statusClosed");

        assertThat(closed.getAttribute("name")).isEqualTo("CLOSED");
        assertThat(closed.getElementsByTagNameNS(BPMN_NS, "outgoing").getLength()).isZero();
    }

    private static Document loadTicketLifecycleModel() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        try (InputStream input = TicketLifecycleBpmnModelTests.class
                .getResourceAsStream("/processes/ticket-lifecycle.bpmn2")) {
            assertThat(input).isNotNull();
            return factory.newDocumentBuilder().parse(input);
        }
    }

    private static Element elementById(Document document, String localName, String id) {
        NodeList elements = document.getElementsByTagNameNS(BPMN_NS, localName);
        for (int index = 0; index < elements.getLength(); index++) {
            Element element = (Element) elements.item(index);
            if (id.equals(element.getAttribute("id"))) {
                return element;
            }
        }
        throw new AssertionError("BPMN element not found: " + localName + "#" + id);
    }

    private static Set<String> signalNames(Document document) {
        NodeList signals = document.getElementsByTagNameNS(BPMN_NS, "signal");
        Set<String> names = new HashSet<>();
        for (int index = 0; index < signals.getLength(); index++) {
            names.add(((Element) signals.item(index)).getAttribute("name"));
        }
        return names;
    }

    private static Set<String> sequenceFlows(Document document) {
        NodeList sequenceFlows = document.getElementsByTagNameNS(BPMN_NS, "sequenceFlow");
        Set<String> flows = new HashSet<>();
        for (int index = 0; index < sequenceFlows.getLength(); index++) {
            Element flow = (Element) sequenceFlows.item(index);
            flows.add(flow.getAttribute("sourceRef") + "->" + flow.getAttribute("targetRef"));
        }
        return flows;
    }
}

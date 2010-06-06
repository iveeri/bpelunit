package org.bpelunit.utils.datasourceinliner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.bpelunit.framework.control.datasource.DataSourceUtil;
import org.bpelunit.framework.control.ext.IDataSource;
import org.bpelunit.framework.xml.suite.XMLDataSource;
import org.bpelunit.framework.xml.suite.XMLTestCase;
import org.bpelunit.framework.xml.suite.XMLTestSuite;
import org.bpelunit.framework.xml.suite.XMLTestSuiteDocument;
import org.bpelunit.test.util.TestTestRunner;
import org.bpelunit.test.util.TestUtil;
import org.junit.Test;

/**
 * Tests for the data source inliner.
 * 
 * @author Antonio García-Domínguez
 */
public class DataSourceInlinerTest {

    private static final File BPTS_BASEDIR       = new File("resources");

    private static final File LINKED_BPTS_FILE   = getBPTSFile("linkedDS",
                                                         "tacService-suitesrc-t.bpts");
    private static final File EMBEDDED_BPTS_FILE = getBPTSFile("embeddedDS",
                                                         "tacService-casesrc-t.bpts");
    private static final File DELAYSEQ_BPTS_FILE = getBPTSFile("delaySequence",
                                                         "LoanServiceTestSuite.bpts");
    private static final File PLAIN_BPTS_FILE    = getBPTSFile("plain",
                                                         "WastePaperBasketTestSuite.bpts");

    @Test
    public void inlinedPlainBPTSIsUnchanged() throws Exception {
        assertInlinedIsUnchanged(PLAIN_BPTS_FILE);
    }

    @Test
    public void inlinedDelaySequenceIsUnchanged() throws Exception {
        assertInlinedIsUnchanged(DELAYSEQ_BPTS_FILE);
    }

    @Test
    public void inlinedEmbeddedDSIsEquivalent() throws Exception {
        assertInlinedProducesSameResult(EMBEDDED_BPTS_FILE);
    }

    @Test
    public void inlinedEmbeddedDSIsStandalone() throws Exception {
        assertInlinedIsStandalone(EMBEDDED_BPTS_FILE);
    }

    @Test
    public void inlinedLinkedDSIsEquivalent() throws Exception {
        assertInlinedProducesSameResult(LINKED_BPTS_FILE);
    }

    @Test
    public void inlinedLinkedDSIsStandalone() throws Exception {
        assertInlinedIsStandalone(LINKED_BPTS_FILE);
    }

    private static File getBPTSFile(final String dirBasename,
            final String bptsBasename) {
        return new File(new File(BPTS_BASEDIR, dirBasename), bptsBasename);
    }

    private void assertInlinedIsUnchanged(final File fOriginal)
            throws IOException, XmlException {
        final File fInlined = inlineToTemp(fOriginal);
        XMLTestSuiteDocument docOriginal = XMLTestSuiteDocument.Factory
                .parse(fOriginal);
        XMLTestSuiteDocument docInlined = XMLTestSuiteDocument.Factory
                .parse(fInlined);
        assertEquals("Plain BPTS should be unchanged after inlining",
                docOriginal.xmlText(), docInlined.xmlText());
    }

    private File assertInlinedProducesSameResult(final File fOriginal)
            throws IOException, XmlException, Exception {
        final File fInlined = inlineToTemp(fOriginal);
        TestUtil.assertSameAndSuccessfulResults(
                "BPTS with no templates should work just like before",
                fOriginal, fInlined);
        return fInlined;
    }

    private void assertInlinedIsStandalone(final File fOriginal) throws Exception {
        final XMLTestSuiteDocument xmlOriginal
            = XMLTestSuiteDocument.Factory.parse(fOriginal);
        final XMLTestSuiteDocument xmlInlined
            = new Inliner().inlineDataSources(xmlOriginal);

        final XMLTestSuite xmlTestSuite = xmlInlined.getTestSuite();
        if (xmlTestSuite.isSetSetUp()) {
            assertFalse("Inlined BPTS should not have a global data source",
                    xmlTestSuite.getSetUp().isSetDataSource());
        }

        final TestTestRunner runner = new TestTestRunner(fOriginal);
        for (XMLTestCase xmlTestCase : xmlTestSuite.getTestCases().getTestCaseList()) {
            if (xmlTestCase.isSetSetUp() && xmlTestCase.getSetUp().isSetDataSource()) {
                final XMLDataSource xmlDataSrc = xmlTestCase.getSetUp().getDataSource();
                assertFalse("Inlined BPTS should only have embedded data sources",
                            xmlDataSrc.isSetSrc());

                IDataSource dataSource
                    = DataSourceUtil.createDataSource(
                            xmlTestSuite, xmlTestCase,
                            fOriginal.getParentFile(), runner);
                assertEquals(
                        "Data sources in each test case of an inlined BPTS should only have one row",
                        1, dataSource.getNumberOfRows());
            }
        }
    }

    private File inlineToTemp(final File fOriginal) throws IOException,
            XmlException {
        final File fInlined = File.createTempFile("inlined", ".bpts", fOriginal
                .getParentFile());
        fInlined.deleteOnExit();
        new Inliner().inlineFile(fOriginal, fInlined);
        return fInlined;
    }
}
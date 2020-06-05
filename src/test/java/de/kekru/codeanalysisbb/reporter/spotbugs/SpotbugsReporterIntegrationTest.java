package de.kekru.codeanalysisbb.reporter.spotbugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cdancy.bitbucket.rest.domain.insights.Annotation;
import com.cdancy.bitbucket.rest.domain.insights.Annotation.AnnotationSeverity;
import com.cdancy.bitbucket.rest.domain.insights.Annotation.AnnotationType;
import com.cdancy.bitbucket.rest.options.CreateAnnotations;
import com.cdancy.bitbucket.rest.options.CreateInsightReport;
import com.cdancy.bitbucket.rest.options.CreateInsightReport.RESULT;
import de.kekru.codeanalysisbb.CodeAnalysisBitbucketExporter;
import de.kekru.codeanalysisbb.testutils.AbstractIntegrationTest;
import de.kekru.codeanalysisbb.utils.CodeAnalysisBitbucketException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SpotbugsReporterIntegrationTest extends AbstractIntegrationTest {

  public SpotbugsReporterIntegrationTest() {
    super("src/test/resources/spotbugs/01/config.yml");
  }

  @Test
  public void testSpotbugsReporting() {
    try {
      new CodeAnalysisBitbucketExporter(serviceRegistry);
      fail("Expected CodeAnalysisBitbucketException");
    } catch (CodeAnalysisBitbucketException e) {
      assertEquals("There are failed reports: spotbugs-key", e.getMessage());
    }

    ArgumentCaptor<CreateInsightReport> insightReportCaptor = ArgumentCaptor
        .forClass(CreateInsightReport.class);
    ArgumentCaptor<CreateAnnotations> annotationsCaptor = ArgumentCaptor
        .forClass(CreateAnnotations.class);

    verify(insightsApiMock, times(1)).createReport(
        eq("some-project"),
        eq("some-repo"),
        eq("7281682ec6f7a47f3da5eb2afee52eba9044bb28"),
        eq("spotbugs-key"),
        insightReportCaptor.capture());

    verify(insightsApiMock, times(1)).createAnnotations(
        eq("some-project"),
        eq("some-repo"),
        eq("7281682ec6f7a47f3da5eb2afee52eba9044bb28"),
        eq("spotbugs-key"),
        annotationsCaptor.capture());


    // Check report
    CreateInsightReport report = insightReportCaptor.getValue();
    assertEquals("Spotbugs Code Analysis Report", report.title());
    assertEquals("Spotbugs", report.reporter());
    assertEquals("Overall report summary:\n\nFindings by Severity:\nMEDIUM findings: 14\nHIGH findings: 2\n\nThese findings are divided into these types:\nCODE_SMELL findings: 14\nBUG findings: 2\n\nBelow only findings in changed lines are listed\n", report.details());
    assertEquals("https://spotbugs.github.io", report.link());
    assertEquals("https://spotbugs.github.io/images/logos/spotbugs_icon_only_zoom_256px.png", report.logoUrl());
    assertEquals(0, report.data().size());
    assertEquals(RESULT.FAIL, report.result());

    // Check annotations
    List<Annotation> annotations = annotationsCaptor.getValue()
        .annotations()
        .stream()
        .sorted(Comparator
            .comparing(Annotation::path)
            .thenComparing(Comparator.comparing(Annotation::line))
            .thenComparing(Comparator.comparing(Annotation::message)))
        .collect(Collectors.toList());
    assertEquals(16, annotations.size());

    Annotation a = annotations.get(0);
    assertNotNull(a.reportKey());
    assertNotNull(a.externalId());
    assertEquals("https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html#dodgy-code-style", a.link());
    assertEquals("Dead store to abc in new de.kekru.example.codeanalysisbb.Main()", a.message().trim());
    assertEquals("src/main/java/de/kekru/example/codeanalysisbb/Main.java", a.path());
    assertEquals(33, a.line());
    assertEquals(AnnotationSeverity.MEDIUM, a.severity());
    assertEquals(AnnotationType.CODE_SMELL, a.type());

    a = annotations.get(4);
    assertNotNull(a.reportKey());
    assertNotNull(a.externalId());
    assertEquals("https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html#dodgy-code-style", a.link());
    assertEquals("Read of unwritten public or protected field something in de.kekru.example.codeanalysisbb.SecondClass.getSomething()", a.message().trim());
    assertEquals("src/main/java/de/kekru/example/codeanalysisbb/SecondClass.java", a.path());
    assertEquals(14, a.line());
    assertEquals(AnnotationSeverity.HIGH, a.severity());
    assertEquals(AnnotationType.CODE_SMELL, a.type());

    // same as get(4) above, but with path in src/main/resources/...
    a = annotations.get(10);
    assertNotNull(a.reportKey());
    assertNotNull(a.externalId());
    assertEquals("https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html#dodgy-code-style", a.link());
    assertEquals("Read of unwritten public or protected field something in de.kekru.example.codeanalysisbb.SecondClass.getSomething()", a.message().trim());
    assertEquals("src/main/resources/de/kekru/example/codeanalysisbb/SecondClass.java", a.path());
    assertEquals(14, a.line());
    assertEquals(AnnotationSeverity.HIGH, a.severity());
    assertEquals(AnnotationType.CODE_SMELL, a.type());

    a = annotations.get(12);
    assertNotNull(a.reportKey());
    assertNotNull(a.externalId());
    assertEquals("https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html#experimental-experimental", a.link());
    assertEquals("de.kekru.example.codeanalysisbb.MainTest.keepOpenStream() may fail to clean up java.io.InputStream", a.message().trim());
    assertEquals("src/test/java/de/kekru/example/codeanalysisbb/MainTest.java", a.path());
    assertEquals(20, a.line());
    assertEquals(AnnotationSeverity.MEDIUM, a.severity());
    assertEquals(AnnotationType.CODE_SMELL, a.type());
  }

}

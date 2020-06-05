package de.kekru.codeanalysisbb.reporter.pmd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PmdReporterIntegrationTest extends AbstractIntegrationTest {

  public PmdReporterIntegrationTest() {
    super("src/test/resources/pmd/01/config.yml");
  }

  @Test
  public void testPmdReporting() {
    new CodeAnalysisBitbucketExporter(serviceRegistry);

    ArgumentCaptor<CreateInsightReport> insightReportCaptor = ArgumentCaptor
        .forClass(CreateInsightReport.class);
    ArgumentCaptor<CreateAnnotations> annotationsCaptor = ArgumentCaptor
        .forClass(CreateAnnotations.class);

    verify(insightsApiMock, times(1)).createReport(
        eq("some-project"),
        eq("some-repo"),
        eq("7281682ec6f7a47f3da5eb2afee52eba9044bb28"),
        eq("pmd-key"),
        insightReportCaptor.capture());

    verify(insightsApiMock, times(1)).createAnnotations(
        eq("some-project"),
        eq("some-repo"),
        eq("7281682ec6f7a47f3da5eb2afee52eba9044bb28"),
        eq("pmd-key"),
        annotationsCaptor.capture());


    // Check report
    CreateInsightReport report = insightReportCaptor.getValue();
    assertEquals("PMD Code Analysis Report", report.title());
    assertEquals("PMD", report.reporter());
    assertEquals("Overall report summary:\n\nFindings by Severity:\nLOW findings: 10\nMEDIUM findings: 4\nHIGH findings: 4\n\nThese findings are divided into these types:\nCODE_SMELL findings: 14\nBUG findings: 4\n\nBelow only findings in changed lines are listed\n", report.details());
    assertEquals("https://pmd.github.io", report.link());
    assertEquals("https://pmd.github.io/img/pmd_logo.png", report.logoUrl());
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
    assertEquals(18, annotations.size());

    Annotation a = annotations.get(0);
    assertNotNull(a.reportKey());
    assertNotNull(a.externalId());
    assertEquals("https://pmd.github.io/pmd-6.21.0/pmd_rules_java_bestpractices.html#unusedimports", a.link());
    assertEquals("Avoid unused imports such as 'com.cdancy.bitbucket.rest.BitbucketClient' (UnusedImports)", a.message().trim());
    assertEquals("src/main/java/de/kekru/nociplugin/bitbucket/BitbucketService.java", a.path());
    assertEquals(3, a.line());
    assertEquals(AnnotationSeverity.LOW, a.severity());
    assertEquals(AnnotationType.CODE_SMELL, a.type());

    a = annotations.get(17);
    assertNotNull(a.reportKey());
    assertNotNull(a.externalId());
    assertEquals("https://pmd.github.io/pmd-6.21.0/pmd_rules_java_bestpractices.html#guardlogstatement", a.link());
    assertEquals("Logger calls should be surrounded by log level guards. (GuardLogStatement)", a.message().trim());
    assertEquals("src/test/java/de/kekru/nociplugin/testutils/TestShellExecutor.java", a.path());
    assertEquals(23, a.line());
    assertEquals(AnnotationSeverity.HIGH, a.severity());
    assertEquals(AnnotationType.CODE_SMELL, a.type());

  }

}

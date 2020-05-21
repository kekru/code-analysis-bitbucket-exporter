package de.kekru.codeanalysisbb.reporter;

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
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
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
    assertEquals("PMD Report", report.details());
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

    Annotation a00 = annotations.get(0);
    assertNotNull(a00.reportKey());
    assertNotNull(a00.externalId());
    assertEquals("https://pmd.github.io/pmd-6.21.0/pmd_rules_java_bestpractices.html#unusedimports", a00.link());
    assertEquals("Avoid unused imports such as 'com.cdancy.bitbucket.rest.BitbucketClient' (UnusedImports)", a00.message().trim());
    assertEquals("src/main/java/de/kekru/nociplugin/bitbucket/BitbucketService.java", a00.path());
    assertEquals(3, a00.line());
    assertEquals(AnnotationSeverity.LOW, a00.severity());
    assertEquals(AnnotationType.CODE_SMELL, a00.type());

    Annotation a17 = annotations.get(17);
    assertNotNull(a17.reportKey());
    assertNotNull(a17.externalId());
    assertEquals("https://pmd.github.io/pmd-6.21.0/pmd_rules_java_bestpractices.html#guardlogstatement", a17.link());
    assertEquals("Logger calls should be surrounded by log level guards. (GuardLogStatement)", a17.message().trim());
    assertEquals("src/test/java/de/kekru/nociplugin/testutils/TestShellExecutor.java", a17.path());
    assertEquals(23, a17.line());
    assertEquals(AnnotationSeverity.HIGH, a17.severity());
    assertEquals(AnnotationType.CODE_SMELL, a17.type());

  }

}

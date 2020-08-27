package de.kekru.codeanalysisbb.reporter.sonarqube;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cdancy.bitbucket.rest.domain.insights.Annotation;
import com.cdancy.bitbucket.rest.domain.insights.Annotation.AnnotationSeverity;
import com.cdancy.bitbucket.rest.domain.insights.Annotation.AnnotationType;
import com.cdancy.bitbucket.rest.options.CreateAnnotations;
import com.cdancy.bitbucket.rest.options.CreateInsightReport;
import com.cdancy.bitbucket.rest.options.CreateInsightReport.RESULT;
import com.google.gson.Gson;
import de.kekru.codeanalysisbb.CodeAnalysisBitbucketExporter;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.SonarIssuesAndComponents;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.qualitygates.SonarProjectStatus;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.qualitygates.SonarProjectStatusResult;
import de.kekru.codeanalysisbb.testutils.AbstractIntegrationTest;
import de.kekru.javautils.mockitohelper.MockitoHelper;
import java.io.FileReader;
import java.io.Reader;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SonarqubeReporterIntegrationTest extends AbstractIntegrationTest {

  SonarProjectStatus sonarProjectStatus;
  SonarIssuesAndComponents sonarIssuesAndComponents;

  public SonarqubeReporterIntegrationTest() {
    super("src/test/resources/sonarqube/01/config.yml");
    initSonarqubeApiServiceMock();

    sonarProjectStatus = readFromJson(
        "src/test/resources/sonarqube/01/mock-api/project-status.json",
        SonarProjectStatusResult.class
    ).getProjectStatus();

    sonarIssuesAndComponents = readFromJson(
        "src/test/resources/sonarqube/01/mock-api/issues-and-components.json",
        SonarIssuesAndComponents.class
    );
  }

  @Test
  public void testSonarqubeReporting() {
    new CodeAnalysisBitbucketExporter(serviceRegistry);
    ArgumentCaptor<CreateInsightReport> insightReportCaptor = ArgumentCaptor
        .forClass(CreateInsightReport.class);
    ArgumentCaptor<CreateAnnotations> annotationsCaptor = ArgumentCaptor
        .forClass(CreateAnnotations.class);

    verify(insightsApiMock, times(1)).createReport(
        eq("some-project"),
        eq("some-repo"),
        eq("7281682ec6f7a47f3da5eb2afee52eba9044bb28"),
        eq("sonar-key"),
        insightReportCaptor.capture());

    verify(insightsApiMock, times(1)).createAnnotations(
        eq("some-project"),
        eq("some-repo"),
        eq("7281682ec6f7a47f3da5eb2afee52eba9044bb28"),
        eq("sonar-key"),
        annotationsCaptor.capture());


    // Check report
    CreateInsightReport report = insightReportCaptor.getValue();
    assertEquals("Sonarqube Report", report.title());
    assertEquals("Sonarqube", report.reporter());
    assertEquals("Overall report summary:\n\nFindings by Severity:\nLOW findings: 4\nMEDIUM findings: 4\nHIGH findings: 3\n\nThese findings are divided into these types:\nVULNERABILITY findings: 1\nCODE_SMELL findings: 7\nBUG findings: 3\n\nBelow only findings in changed lines are listed\n", report.details());
    assertEquals("https://sonarqube.localhost", report.link());
    assertEquals("https://docs.sonarqube.org/latest/images/SonarQubeIcon.svg", report.logoUrl());
    assertEquals(0, report.data().size());
    assertEquals(RESULT.PASS, report.result());

    // Check annotations
    List<Annotation> annotations = annotationsCaptor.getValue()
        .annotations()
        .stream()
        .sorted(Comparator
            .comparing(Annotation::path)
            .thenComparing(Comparator.comparing(Annotation::line))
            .thenComparing(Comparator.comparing(Annotation::message)))
        .collect(Collectors.toList());
    assertEquals(11, annotations.size());

    Annotation a = annotations.get(0);
    assertNotNull(a.reportKey());
    assertNotNull(a.externalId());
    assertEquals("https://sonarqube.localhost/project/issues?id=example-code-analysis-bitbucket-exporter&open=AXKPhF-kfWqOQR8QowS3", a.link());
    assertEquals("Remove this unused import 'java.io.FileNotFoundException'. (java:S1128)", a.message().trim());
    assertEquals("src/main/java/de/kekru/example/codeanalysisbb/Main.java", a.path());
    assertEquals(5, a.line());
    assertEquals(AnnotationSeverity.LOW, a.severity());
    assertEquals(AnnotationType.CODE_SMELL, a.type());

    a = annotations.get(6);
    assertNotNull(a.reportKey());
    assertNotNull(a.externalId());
    assertEquals("https://sonarqube.localhost/project/issues?id=example-code-analysis-bitbucket-exporter&open=AXKPhF-kfWqOQR8QowS1", a.link());
    assertEquals("Use try-with-resources or close this \"FileInputStream\" in a \"finally\" clause. (java:S2095)", a.message().trim());
    assertEquals("src/main/java/de/kekru/example/codeanalysisbb/Main.java", a.path());
    assertEquals(31, a.line());
    assertEquals(AnnotationSeverity.HIGH, a.severity());
    assertEquals(AnnotationType.BUG, a.type());

    a = annotations.get(7);
    assertNotNull(a.reportKey());
    assertNotNull(a.externalId());
    assertEquals("https://sonarqube.localhost/project/issues?id=example-code-analysis-bitbucket-exporter&open=AXKPhF-afWqOQR8QowSz", a.link());
    assertEquals("Make something a static final constant or non-public and provide accessors if needed. (java:S1104)", a.message().trim());
    assertEquals("src/main/java/de/kekru/example/codeanalysisbb/SecondClass.java", a.path());
    assertEquals(7, a.line());
    assertEquals(AnnotationSeverity.LOW, a.severity());
    assertEquals(AnnotationType.VULNERABILITY, a.type());

    a = annotations.get(10);
    assertNotNull(a.reportKey());
    assertNotNull(a.externalId());
    assertEquals("https://sonarqube.localhost/project/issues?id=example-code-analysis-bitbucket-exporter&open=AXKPhF8vfWqOQR8QowSx", a.link());
    assertEquals("Add at least one assertion to this test case. (java:S2699)", a.message().trim());
    assertEquals("src/test/java/de/kekru/example/codeanalysisbb/MainTest.java", a.path());
    assertEquals(13, a.line());
    assertEquals(AnnotationSeverity.HIGH, a.severity());
    assertEquals(AnnotationType.CODE_SMELL, a.type());
  }

  private void initSonarqubeApiServiceMock() {
    SonarqubeApiService sonarqubeApiServiceMock = mock(SonarqubeApiService.class);

    MockitoHelper.doAnswer(() -> sonarProjectStatus)
        .when(sonarqubeApiServiceMock)
        .readProjectStatus();

    MockitoHelper.doAnswer(() -> sonarIssuesAndComponents)
        .when(sonarqubeApiServiceMock)
        .readSonarIssues();

    serviceRegistry.overrideService(SonarqubeApiService.class, sonarqubeApiServiceMock);
  }

  private <T> T readFromJson(String filename, Class<T> targetClass) {
    Gson gson = new Gson();
    try (Reader reader = new FileReader(new java.io.File(filename))) {
      return gson.fromJson(reader, targetClass);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}

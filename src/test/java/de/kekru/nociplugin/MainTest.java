package de.kekru.nociplugin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cdancy.bitbucket.rest.domain.insights.Annotation;
import com.cdancy.bitbucket.rest.options.CreateAnnotations;
import com.cdancy.bitbucket.rest.options.CreateInsightReport;
import com.cdancy.bitbucket.rest.options.CreateInsightReport.RESULT;
import de.kekru.nociplugin.testutils.AbstractIntegrationTest;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class MainTest extends AbstractIntegrationTest {

  public MainTest() {
    super("src/test/resources/pmd/01/config.yml");
  }

  @Test
  public void test() {
    new Main(serviceRegistry);

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
    assertEquals(RESULT.PASS, report.result());

    // Check annotations
    List<Annotation> annotations = annotationsCaptor.getValue().annotations();
    assertEquals(18, annotations.size());
  }

}

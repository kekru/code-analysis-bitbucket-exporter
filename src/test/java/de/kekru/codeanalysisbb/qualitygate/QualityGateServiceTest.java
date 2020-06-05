package de.kekru.codeanalysisbb.qualitygate;

import static com.cdancy.bitbucket.rest.options.CreateInsightReport.RESULT.FAIL;
import static com.cdancy.bitbucket.rest.options.CreateInsightReport.RESULT.PASS;
import static org.junit.Assert.assertEquals;

import com.cdancy.bitbucket.rest.options.CreateInsightReport.RESULT;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation.BitbucketSeverity;
import de.kekru.codeanalysisbb.config.Config.QualityGateThreshold;
import de.kekru.codeanalysisbb.testutils.AbstractIntegrationTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class QualityGateServiceTest extends AbstractIntegrationTest {

  QualityGateService qualityGateService;

  public QualityGateServiceTest() {
    super(""); // no config file needed
  }

  @Parameters(name = "{index}: found {0},{1},{2} and threshold {3},{4},{5}, expect {6}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            0, // findings low
            0, // findings medium
            0, // findings high
            0, // threshold low
            0, // threshold medium
            0, // threshold high
            PASS // result
        },
        {
            0, // findings low
            0, // findings medium
            0, // findings high
            null, // threshold low
            null, // threshold medium
            null, // threshold high
            PASS // result
        },
        {
            0, // findings low
            0, // findings medium
            0, // findings high
            -1, // threshold low
            -100, // threshold medium
            -1, // threshold high
            PASS // result
        },
        {
            1, // findings low
            0, // findings medium
            0, // findings high
            0, // threshold low
            0, // threshold medium
            0, // threshold high
            FAIL // result
        },
        {
            0, // findings low
            0, // findings medium
            0, // findings high
            1, // threshold low
            1, // threshold medium
            1, // threshold high
            PASS // result
        },
        {
            1, // findings low
            2, // findings medium
            3, // findings high
            2, // threshold low
            3, // threshold medium
            4, // threshold high
            PASS // result
        },
        {
            1, // findings low
            2, // findings medium
            3, // findings high
            1, // threshold low
            2, // threshold medium
            3, // threshold high
            FAIL // result
        },
        {
            0, // findings low
            2, // findings medium
            0, // findings high
            0, // threshold low
            0, // threshold medium
            1, // threshold high
            FAIL // result
        },
        {
            0, // findings low
            0, // findings medium
            2, // findings high
            0, // threshold low
            0, // threshold medium
            1, // threshold high
            FAIL // result
        },
        {
            1, // findings low
            2, // findings medium
            4, // findings high
            0, // threshold low
            3, // threshold medium
            3, // threshold high
            FAIL // result
        },
        {
            50, // findings low
            60, // findings medium
            70, // findings high
            49, // threshold low
            59, // threshold medium
            69, // threshold high
            FAIL // result
        },
        {
            50, // findings low
            60, // findings medium
            70, // findings high
            51, // threshold low
            61, // threshold medium
            71, // threshold high
            PASS // result
        },
        {
            50, // findings low
            60, // findings medium
            70, // findings high
            50, // threshold low
            0, // threshold medium
            0, // threshold high
            FAIL // result
        },
    });
  }

  @Parameter(0)
  public Integer findingsLow;

  @Parameter(1)
  public Integer findingsMedium;

  @Parameter(2)
  public Integer findingsHigh;

  @Parameter(3)
  public Integer thresholdLow;

  @Parameter(4)
  public Integer thresholdMedium;

  @Parameter(5)
  public Integer thresholdHigh;

  @Parameter(6)
  public RESULT expectedResult;

  @Before
  public void init() {
    qualityGateService = serviceRegistry.get(QualityGateService.class);
  }

  @Test
  public void test () {
    // Given
    List<BitbucketAnnotation> annotations = new LinkedList<>();
    IntStream.range(0, findingsLow)
        .mapToObj(i -> BitbucketAnnotation.builder().severity(BitbucketSeverity.LOW).build())
        .forEach(annotations::add);

    IntStream.range(0, findingsMedium)
        .mapToObj(i -> BitbucketAnnotation.builder().severity(BitbucketSeverity.MEDIUM).build())
        .forEach(annotations::add);

    IntStream.range(0, findingsHigh)
        .mapToObj(i -> BitbucketAnnotation.builder().severity(BitbucketSeverity.HIGH).build())
        .forEach(annotations::add);

    QualityGateThreshold threshold = new QualityGateThreshold();
    threshold.setLowCount(thresholdLow);
    threshold.setMediumCount(thresholdMedium);
    threshold.setHighCount(thresholdHigh);

    // When
    RESULT result = qualityGateService.getQualityGateResult(annotations, threshold);

    // Then
    assertEquals(expectedResult, result);
  }
}

package de.kekru.codeanalysisbb.testutils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

import com.cdancy.bitbucket.rest.domain.common.RequestStatus;
import com.cdancy.bitbucket.rest.domain.insights.InsightReport;
import com.cdancy.bitbucket.rest.features.InsightsApi;
import com.cdancy.bitbucket.rest.options.CreateAnnotations;
import com.cdancy.bitbucket.rest.options.CreateInsightReport;
import de.kekru.codeanalysisbb.bitbucket.BitbucketThirdPartyService;
import de.kekru.codeanalysisbb.shell.ShellExecutorService;
import de.kekru.javautils.mockitohelper.MockitoHelper;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class AbstractIntegrationTest {

  private final String configFile;

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private static final Logger LOG = LoggerFactory.getLogger(AbstractIntegrationTest.class);
  protected TestServiceRegistry serviceRegistry = new TestServiceRegistry();

  @Mock
  public InsightsApi insightsApiMock;

  @Mock
  public InsightReport insightsReportResponseMock;

  @Mock
  public RequestStatus annotationsResponseMock;

  @Before
  public void initAbstractIntegrationTest() {
    MockitoAnnotations.initMocks(this);
    System.setProperty("configFile", configFile);
    initBitbucketThirdPartyServiceMock();
    initTestShellExecutor();
  }

  private void initBitbucketThirdPartyServiceMock() {
    BitbucketThirdPartyService bitbucketThirdPartyService = mock(BitbucketThirdPartyService.class);

    MockitoHelper.doAnswer(() -> insightsApiMock)
        .when(bitbucketThirdPartyService)
        .getInsightsApi();

    MockitoHelper.doAnswer(() -> insightsReportResponseMock)
        .when(insightsApiMock)
        .createReport(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(CreateInsightReport.class)
        );

    MockitoHelper.doAnswer(() -> annotationsResponseMock)
        .when(insightsApiMock)
        .createAnnotations(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            any(CreateAnnotations.class)
        );

    serviceRegistry.overrideService(BitbucketThirdPartyService.class, bitbucketThirdPartyService);
  }

  private void initTestShellExecutor() {
    TestShellExecutor testShellExecutor = new TestShellExecutor();
    testShellExecutor.setMockedGitCommitId("7281682ec6f7a47f3da5eb2afee52eba9044bb28");

    serviceRegistry.overrideService(ShellExecutorService.class, testShellExecutor);
  }
}

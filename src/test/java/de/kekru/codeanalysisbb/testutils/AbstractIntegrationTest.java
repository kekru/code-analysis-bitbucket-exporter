package de.kekru.codeanalysisbb.testutils;

import static org.mockito.Mockito.mock;

import com.cdancy.bitbucket.rest.features.InsightsApi;
import de.kekru.codeanalysisbb.bitbucket.BitbucketThirdPartyService;
import de.kekru.codeanalysisbb.shell.ShellExecutorService;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
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

  @Before
  public void initAbstractIntegrationTest() {
    System.setProperty("configFile", configFile);
    initBitbucketThirdPartyServiceMock();
    initTestShellExecutor();
  }

  private void initBitbucketThirdPartyServiceMock() {
    BitbucketThirdPartyService bitbucketThirdPartyService = mock(BitbucketThirdPartyService.class);
    TestUtils.doAnswer(() -> insightsApiMock).when(bitbucketThirdPartyService).getInsightsApi();

    serviceRegistry.overrideService(BitbucketThirdPartyService.class, bitbucketThirdPartyService);
  }

  private void initTestShellExecutor() {
    TestShellExecutor testShellExecutor = new TestShellExecutor();
    testShellExecutor.setMockedGitCommitId("7281682ec6f7a47f3da5eb2afee52eba9044bb28");

    serviceRegistry.overrideService(ShellExecutorService.class, testShellExecutor);
  }
}

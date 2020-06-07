package de.kekru.codeanalysisbb.reporter.sonarqube;

import de.kekru.codeanalysisbb.testutils.AbstractIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SonarqubeApiServiceTest extends AbstractIntegrationTest {

  public SonarqubeApiServiceTest() {
    super("src/test/resources/sonarqube/01/config.yml");
  }

  @Test
  public void test () {
    SonarqubeApiService downloader = serviceRegistry.get(SonarqubeApiService.class);
    downloader.readSonarIssues();
    downloader.readProjectStatus();
  }
}

package de.kekru.codeanalysisbb.reporter.sonarqube;

import static org.junit.Assert.*;

import de.kekru.codeanalysisbb.testutils.AbstractIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SonarqubeDownloaderServiceTest extends AbstractIntegrationTest {

  public SonarqubeDownloaderServiceTest() {
    super("src/test/resources/sonarqube/01/config.yml");
  }

  @Test
  public void test () {
    SonarqubeDownloaderService downloader = serviceRegistry.get(SonarqubeDownloaderService.class);
    downloader.readSonarIssues();
  }
}

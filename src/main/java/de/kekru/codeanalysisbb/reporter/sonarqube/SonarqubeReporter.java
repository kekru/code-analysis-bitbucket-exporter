package de.kekru.codeanalysisbb.reporter.sonarqube;

import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketReport;
import de.kekru.codeanalysisbb.reporter.interf.Reporter;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.SonarIssuesAndComponents;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.SonarqubeMetaInfoService;
import de.kekru.javautils.dependencyinjection.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SonarqubeReporter implements Reporter {

  private final SonarqubeApiService sonarqubeApiService;

  @Override
  public BitbucketReport getBitbucketReport() {


    sonarqubeApiService.readProjectStatus();

    SonarIssuesAndComponents issues = sonarqubeApiService
        .readSonarIssues();

    return null;
  }
}

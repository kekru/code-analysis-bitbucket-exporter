package de.kekru.codeanalysisbb.reporter.sonarqube;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import de.kekru.codeanalysisbb.config.Config;
import de.kekru.codeanalysisbb.config.Config.SonarConfig;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.SonarqubeMetaInfoService;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.SonarIssuesAndComponents;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.SonarqubeApi;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.issues.SonarComponent;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.issues.SonarIssue;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.issues.SonarSearch;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.qualitygates.SonarProjectStatus;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.jcloudsconfig.SonarApiMetadata;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.jcloudsconfig.SonarqubeAuthenticationModule;
import de.kekru.javautils.dependencyinjection.Service;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jclouds.ContextBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class SonarqubeApiService {

  private Logger LOG = LoggerFactory.getLogger(SonarqubeApiService.class);
  private final Config config;
  private final SonarqubeMetaInfoService sonarqubeMetaInfoService;
  private SonarqubeApi sonarqubeApi;

  public SonarProjectStatus readProjectStatus () {
    String analysisId = sonarqubeMetaInfoService.getAnalysisId();
    SonarProjectStatus status = getSonarqubeApi().qualityGatesApi().getProjectStatus(analysisId)
        .getProjectStatus();
    LOG.debug("Retrieved sonar project status: " + status);
    return status;
  }

  public SonarIssuesAndComponents readSonarIssues() {
    SonarConfig sonarqubeConfig = config.getReporter().getSonarqube();

    List<SonarIssue> foundIssues = new LinkedList<>();
    List<SonarComponent> foundComponents = new LinkedList<>();
    SonarSearch currentSearch;

    int page = 1;
    do {
      currentSearch = search(page, sonarqubeConfig, getSonarqubeApi());
      foundIssues.addAll(currentSearch.getIssues());
      foundComponents.addAll(currentSearch.getComponents());
      page++;
    } while (!currentSearch.getIssues().isEmpty());

    SonarIssuesAndComponents result = new SonarIssuesAndComponents(
        foundIssues,
        foundComponents
    );

    LOG.debug("Retrieved sonar issues: " + result);

    return result;
  }

  private SonarqubeApi getSonarqubeApi() {
    if (sonarqubeApi != null) {
      return sonarqubeApi;
    }

    SonarConfig sonarqubeConfig = config.getReporter().getSonarqube();

    Iterable<Module> modules = ImmutableSet.of(
        new SLF4JLoggingModule(),
        new SonarqubeAuthenticationModule(sonarqubeConfig.getLogin())
    );

    sonarqubeApi = ContextBuilder
        .newBuilder(new SonarApiMetadata.Builder().build())
        .endpoint(sonarqubeMetaInfoService.getServerUrl())
        .credentials("something", "")
        .modules(modules)
        .buildApi(SonarqubeApi.class);

    return sonarqubeApi;
  }

  private SonarSearch search(int page, SonarConfig sonarqubeConfig, SonarqubeApi sonarqubeApi) {
    return sonarqubeApi
        .issuesApi()
        .search(page, 500, sonarqubeConfig.getBranch(), sonarqubeMetaInfoService.getProjectKey());
  }
}

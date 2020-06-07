package de.kekru.codeanalysisbb.reporter.sonarqube;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import de.kekru.codeanalysisbb.config.Config;
import de.kekru.codeanalysisbb.config.Config.SonarConfig;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.SonarIssuesAndComponents;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.SonarqubeApi;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.SonarComponent;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.SonarIssue;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.SonarSearch;
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
public class SonarqubeDownloaderService {

  private Logger LOG = LoggerFactory.getLogger(SonarqubeDownloaderService.class);
  private final Config config;

  public SonarIssuesAndComponents readSonarIssues() {
    SonarConfig sonarqubeConfig = config.getReporter().getSonarqube();

    Iterable<Module> modules = ImmutableSet.of(
        new SLF4JLoggingModule(),
        new SonarqubeAuthenticationModule(sonarqubeConfig.getLogin())
    );

    SonarqubeApi sonarqubeApi = ContextBuilder
        .newBuilder(new SonarApiMetadata.Builder().build())
        .endpoint(sonarqubeConfig.getHostUrl())
        .credentials("something", "")
        .modules(modules)
        .buildApi(SonarqubeApi.class);

    List<SonarIssue> foundIssues = new LinkedList<>();
    List<SonarComponent> foundComponents = new LinkedList<>();
    SonarSearch currentSearch;

    int page = 1;
    do {
      currentSearch = search(page, sonarqubeConfig, sonarqubeApi);
      foundIssues.addAll(currentSearch.getIssues());
      foundComponents.addAll(currentSearch.getComponents());
      page++;
    } while(!currentSearch.getIssues().isEmpty());

    SonarIssuesAndComponents result = new SonarIssuesAndComponents(
        foundIssues,
        foundComponents
    );

    LOG.debug("Retrieved sonar issues: " + result);

    return result;
  }

  private SonarSearch search(int page, SonarConfig sonarqubeConfig, SonarqubeApi sonarqubeApi) {
    return sonarqubeApi
        .issuesApi()
        .search(page, 500, sonarqubeConfig.getBranch(), sonarqubeConfig.getComponentKey());
  }
}

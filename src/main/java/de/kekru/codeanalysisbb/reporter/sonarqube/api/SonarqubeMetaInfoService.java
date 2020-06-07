package de.kekru.codeanalysisbb.reporter.sonarqube.api;

import de.kekru.codeanalysisbb.config.Config;
import de.kekru.codeanalysisbb.config.Config.SonarConfig;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.SonarqubeMetaInfoFile;
import de.kekru.codeanalysisbb.utils.CodeAnalysisBitbucketException;
import de.kekru.codeanalysisbb.utils.FileService;
import de.kekru.javautils.config.PropertyLoaderService;
import de.kekru.javautils.dependencyinjection.Service;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Service
@RequiredArgsConstructor
public class SonarqubeMetaInfoService {

  private final Config config;
  private final FileService fileService;
  private final PropertyLoaderService propertyLoaderService;

  private SonarqubeMetaInfoFile metaInfo;

  private SonarqubeMetaInfoFile getMetaInfo() {
    if (metaInfo != null) {
      return metaInfo;
    }

    SonarConfig sonarConfig = config.getReporter().getSonarqube();

    if (StringUtils.isBlank(sonarConfig.getReportTaskFile())) {
      throw new CodeAnalysisBitbucketException("reportTaskFile must be provided in config file");
    }

    Map<String, String> propertyMap = fileService
        .readFromFileToMap(getAbsoluteReportTaskFilename());

    metaInfo = new SonarqubeMetaInfoFile();
    propertyLoaderService.applyConfigProperties(metaInfo, propertyMap, ".");

    return metaInfo;
  }

  private String getAbsoluteReportTaskFilename() {
    return config.getWorkDir() + "/" + config.getReporter().getSonarqube().getReportTaskFile();
  }

  public String getServerUrl() {
    String serverUrl = config.getReporter().getSonarqube().getServerUrl();
    if (!StringUtils.isBlank(serverUrl)) {
      return serverUrl;
    }

    serverUrl = getMetaInfo().getProjectKey();

    if (StringUtils.isBlank(serverUrl)) {
      throw new CodeAnalysisBitbucketException(
          "A serverUrl must be set with reporter.sonarqube.serverUrl or in "
              + getAbsoluteReportTaskFilename());
    }

    return serverUrl;
  }

  public String getProjectKey() {
    String projectKey = config.getReporter().getSonarqube().getProjectKey();
    if (!StringUtils.isBlank(projectKey)) {
      return projectKey;
    }

    projectKey = getMetaInfo().getProjectKey();

    if (StringUtils.isBlank(projectKey)) {
      throw new CodeAnalysisBitbucketException(
          "A projectKey must be set with reporter.sonarqube.projectKey or in "
              + getAbsoluteReportTaskFilename());
    }

    return projectKey;
  }

  public String getAnalysisId() {
    String taskId = getMetaInfo().getCeTaskId();
    if (StringUtils.isBlank(taskId)) {
      throw new CodeAnalysisBitbucketException(
          "No ceTaskId found in " + getAbsoluteReportTaskFilename());
    }

    return StringUtils.trim(taskId);
  }
}

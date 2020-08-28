package de.kekru.codeanalysisbb.reporter.sonarqube.api.domain;

import lombok.Data;

@Data
// Content from report-task.txt
public class SonarqubeMetaInfoFile {

  private String projectKey;
  private String serverUrl;
  private String serverVersion;
  private String dashboardUrl;
  private String ceTaskId;
  private String ceTaskUrl;
}

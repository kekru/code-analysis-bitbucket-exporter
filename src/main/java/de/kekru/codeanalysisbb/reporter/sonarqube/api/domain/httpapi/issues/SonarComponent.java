package de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.issues;

import lombok.Data;

@Data
public class SonarComponent {

  private String organization;
  private String key;
  private String uuid;
  private boolean enabled;
  private String qualifier;
  private String name;
  private String longName;
  private String path;
}

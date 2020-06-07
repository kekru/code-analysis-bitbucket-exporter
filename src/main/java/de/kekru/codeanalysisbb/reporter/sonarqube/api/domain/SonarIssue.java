package de.kekru.codeanalysisbb.reporter.sonarqube.api.domain;

import java.util.List;
import lombok.Data;

@Data
public class SonarIssue {

  private String key;
  private String rule;
  private String severity;
  private String component;
  private String project;
  private int line;
  private String hash;
  private SonarTextRange textRange;
  private String status;
  private String message;
  private String effort;
  private String debt;
  private String assignee;
  private String author;
  private List<String> tags;
  private String creationDate;
  private String updateDate;
  private String type;
  private String organization;
  private boolean fromHotspot;
}

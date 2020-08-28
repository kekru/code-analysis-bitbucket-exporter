package de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.qualitygates;

import lombok.Data;

@Data
public class SonarCondition {

  private String status;
  private String metricKey;
  private String comparator;
  private int periodIndex;
  private int errorThreshold;
  private int actualValue;
}

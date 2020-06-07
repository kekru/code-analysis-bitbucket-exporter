package de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.qualitygates;

import lombok.Data;

@Data
public class SonarPeriod {

  private String index;
  private String mode;
  private String date;
}

package de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.qualitygates;

import lombok.Data;

@Data
public class SonarProjectStatusResult {

  private SonarProjectStatus projectStatus;
}

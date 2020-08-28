package de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.issues;

import lombok.Data;

@Data
public class SonarPaging {

  private int pageIndex;
  private int pageSize;
  private int total;
}

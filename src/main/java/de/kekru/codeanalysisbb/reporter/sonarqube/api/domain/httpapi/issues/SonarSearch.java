package de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.issues;

import java.util.List;
import lombok.Data;

@Data
public class SonarSearch {

  private int total;
  private int p;
  private int ps;
  private SonarPaging paging;
  private int effortTotal;
  private int debtTotal;
  private List<SonarIssue> issues;
  private List<SonarComponent> components;
}

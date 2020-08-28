package de.kekru.codeanalysisbb.reporter.sonarqube;

import com.cdancy.bitbucket.rest.options.CreateInsightReport.RESULT;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation.BitbucketSeverity;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketAnnotation.BitbucketType;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketReport;
import de.kekru.codeanalysisbb.config.Config;
import de.kekru.codeanalysisbb.config.Config.SonarConfig;
import de.kekru.codeanalysisbb.reporter.ReporterUtilsService;
import de.kekru.codeanalysisbb.reporter.interf.Reporter;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.SonarqubeMetaInfoService;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.SonarIssuesAndComponents;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.issues.SonarComponent;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.issues.SonarIssue;
import de.kekru.codeanalysisbb.reporter.sonarqube.api.domain.httpapi.qualitygates.SonarProjectStatus;
import de.kekru.javautils.dependencyinjection.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Service
@RequiredArgsConstructor
public class SonarqubeReporter implements Reporter {

  private static final String QUALITYGATE_PASS_STATUS = "OK";
  private static final String ISSUE_STATUS_OPEN = "OPEN";

  private final Config config;
  private final ReporterUtilsService reporterUtils;
  private final SonarqubeApiService sonarqubeApiService;
  private final SonarqubeMetaInfoService metaInfoService;

  @Override
  public BitbucketReport getBitbucketReport() {

    SonarProjectStatus projectStatus = sonarqubeApiService.readProjectStatus();

    SonarIssuesAndComponents issues = sonarqubeApiService
        .readSonarIssues();
    List<BitbucketAnnotation> annotations = getBitbucketAnnotations(issues);

    SonarConfig sonarqubeConfig = config.getReporter().getSonarqube();

    return BitbucketReport.builder()
        .reporterConfig(sonarqubeConfig)
        .details(reporterUtils.getDetailsStringFromAnnotations(annotations))
        .annotations(annotations)
        .result(getQualityGateResult(projectStatus))
        .link(metaInfoService.getServerUrl())
        .logoUrl("https://docs.sonarqube.org/latest/images/SonarQubeIcon.svg")
        .build();
  }

  private List<BitbucketAnnotation> getBitbucketAnnotations(SonarIssuesAndComponents issuesAndComponents) {
    Map<String, SonarComponent> componentByKey =
        issuesAndComponents.getComponents()
            .stream()
            .collect(Collectors.toMap(SonarComponent::getKey, sonarComponent -> sonarComponent));

    return issuesAndComponents.getIssues()
      .stream()
      .filter(this::isOpen)
      .map(issue -> toAnnotation(issue, componentByKey.get(issue.getComponent())))
      .collect(Collectors.toList());
  }

  private BitbucketAnnotation toAnnotation(SonarIssue issue, SonarComponent component) {
    return BitbucketAnnotation.builder()
                .line(issue.getLine())
                .message(createMessage(issue))
                .path(component.getPath())
                .severity(toBitbucketSeverity(issue.getSeverity()))
                .type(toBitbucketType(issue.getType()))
                .link(createLink(issue))
                .build();
  }

  private boolean isOpen(SonarIssue issue) {
    return ISSUE_STATUS_OPEN.equals(StringUtils.trimToEmpty(issue.getStatus()));
  }

  private BitbucketSeverity toBitbucketSeverity(String severity) {
    switch (StringUtils.trimToEmpty(severity)) {
      case "BLOCKER": // fall through
      case "CRITICAL":
        return BitbucketSeverity.HIGH;
      case "MAJOR":
        return BitbucketSeverity.MEDIUM;
      default:
        return BitbucketSeverity.LOW;
    }
  }

  private BitbucketType toBitbucketType(String type) {
    switch (StringUtils.trimToEmpty(type)) {
      case "CODE_SMELL":
        return BitbucketType.CODE_SMELL;
      case "VULNERABILITY":
        return BitbucketType.VULNERABILITY;
      default:
        return BitbucketType.BUG;
    }
  }

  private String createMessage(SonarIssue issue) {
    return String.format("%s (%s)",
        StringUtils.trimToEmpty(issue.getMessage()),
        StringUtils.trimToEmpty(issue.getRule())
    ).replace("\r", "")
     .replace("\n", "");
  }

  private String createLink(SonarIssue issue) {
    return String.format("%s/project/issues?id=%s&open=%s",
        StringUtils.trimToEmpty(StringUtils.removeEnd(metaInfoService.getServerUrl(), "/")),
        StringUtils.trimToEmpty(metaInfoService.getProjectKey()),
        StringUtils.trimToEmpty(issue.getKey())
    );
  }

  private RESULT getQualityGateResult(SonarProjectStatus projectStatus) {
    return QUALITYGATE_PASS_STATUS.equals(projectStatus.getStatus())
        ? RESULT.PASS
        : RESULT.FAIL;
  }
}

package de.kekru.codeanalysisbb.config;

import de.kekru.codeanalysisbb.config.interf.LocalXmlReporterConfig;
import de.kekru.codeanalysisbb.config.interf.ReporterConfig;
import de.kekru.codeanalysisbb.reporter.interf.Reporter;
import de.kekru.codeanalysisbb.reporter.pmd.PmdReporter;
import de.kekru.codeanalysisbb.reporter.sonarqube.SonarqubeReporter;
import de.kekru.codeanalysisbb.reporter.spotbugs.SpotbugsReporter;
import de.kekru.javautils.dependencyinjection.Service;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;

@Data
@Service(providedBy = ConfigProvider.class)
public class Config {

  private String workDir;
  private boolean breakExecutionOnQualityGate = false;
  private BitbucketConfig bitbucket = new BitbucketConfig();
  private ReporterConfigs reporter = new ReporterConfigs();

  @Data
  public static class BitbucketConfig {
    private String endPoint;
    private String token;
    private String project;
    private String repo;
    private String commitId;
  }

  @Data
  public static class ReporterConfigs {
    private PmdConfig pmd;
    private SpotbugsConfig spotbugs;
    private SonarConfig sonarqube;

    public  List<ReporterConfig> getActiveReporters() {
      return Stream.of(pmd, spotbugs, sonarqube)
          .filter(Objects::nonNull)
          .filter(ReporterConfig::isEnabled)
          .collect(Collectors.toList());
    }
  }

  @Data
  public static class PmdConfig implements LocalXmlReporterConfig {
    private List<String> inputXmls = new LinkedList<>();
    private String stripBasePathInputXml;
    private String addBasePathPrefix;
    private boolean enabled = true;
    private String key;
    private String title;
    private String reporter;
    private QualityGateThreshold qualityGate = new QualityGateThreshold();

    @Override
    public Class<? extends Reporter> getReporterService() {
      return PmdReporter.class;
    }
  }

  @Data
  public static class SpotbugsConfig implements LocalXmlReporterConfig {
    private List<String> inputXmls = new LinkedList<>();
    private String stripBasePathInputXml;
    private String addBasePathPrefix;
    private boolean enabled = true;
    private String key;
    private String title;
    private String reporter;
    private QualityGateThreshold qualityGate = new QualityGateThreshold();

    @Override
    public Class<? extends Reporter> getReporterService() {
      return SpotbugsReporter.class;
    }
  }

  @Data
  public static class SonarConfig implements ReporterConfig {
    private boolean enabled = true;
    private String stripBasePathInputXml;
    private String addBasePathPrefix;
    private String key;
    private String title;
    private String reporter;
    private String login;
    private String serverUrl;
    private String projectKey;
    private String reportTaskFile;
    private String branch = "master";

    @Override
    public Class<? extends Reporter> getReporterService() {
      return SonarqubeReporter.class;
    }
  }

  @Data
  public static class QualityGateThreshold {
    private Integer lowCount = null;
    private Integer mediumCount = null;
    private Integer highCount = 1;
  }
}

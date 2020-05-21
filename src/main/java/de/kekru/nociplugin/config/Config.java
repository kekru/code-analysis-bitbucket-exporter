package de.kekru.nociplugin.config;

import de.kekru.nociplugin.config.interf.ReporterConfig;
import de.kekru.nociplugin.reporter.SpotbugsReporter;
import de.kekru.nociplugin.reporter.PmdReporter;
import de.kekru.nociplugin.reporter.interf.Reporter;
import de.kekru.nociplugin.serviceregistry.Service;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;

@Data
@Service(providedBy = ConfigProvider.class)
public class Config {

  private Path workDir;
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

    public  List<ReporterConfig> getActiveReporters() {
      return Stream.of(pmd, spotbugs)
          .filter(Objects::nonNull)
          .filter(ReporterConfig::isEnabled)
          .collect(Collectors.toList());
    }
  }

  @Data
  public static class PmdConfig implements ReporterConfig {
    private List<String> inputXmls = new LinkedList<>();
    private boolean enabled = true;
    private String key;
    private String title;
    private String reporter;
    private String logoUrl;

    @Override
    public Class<? extends Reporter> getReporterService() {
      return PmdReporter.class;
    }
  }

  @Data
  public static class SpotbugsConfig implements ReporterConfig {
    private List<String> inputXmls = new LinkedList<>();
    private boolean enabled = true;
    private String key;
    private String title;
    private String reporter;
    private String logoUrl;

    @Override
    public Class<? extends Reporter> getReporterService() {
      return SpotbugsReporter.class;
    }
  }
}

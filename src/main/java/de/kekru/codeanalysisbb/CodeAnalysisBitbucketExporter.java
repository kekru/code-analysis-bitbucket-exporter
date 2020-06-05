package de.kekru.codeanalysisbb;

import de.kekru.codeanalysisbb.bitbucket.BitbucketService;
import de.kekru.codeanalysisbb.bitbucket.datamodel.BitbucketReport;
import de.kekru.codeanalysisbb.config.Config;
import de.kekru.codeanalysisbb.config.interf.ReporterConfig;
import de.kekru.codeanalysisbb.reporter.interf.Reporter;
import de.kekru.codeanalysisbb.serviceregistry.ServiceRegistry;
import de.kekru.codeanalysisbb.utils.BuildBreakerService;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeAnalysisBitbucketExporter {

  private static final Logger LOG = LoggerFactory.getLogger(CodeAnalysisBitbucketExporter.class);

  public static void main(String[] args) {
    run();
  }

  public static void run() {
    new CodeAnalysisBitbucketExporter(new ServiceRegistry());
  }

  public CodeAnalysisBitbucketExporter(ServiceRegistry serviceRegistry) {

    final List<BitbucketReport> reports = serviceRegistry
        .get(Config.class)
        .getReporter()
        .getActiveReporters()
        .stream()
        .map(ReporterConfig::getReporterService)
        .map(serviceRegistry::get)
        .map(Reporter::getBitbucketReport)
        .map(report -> {
          if (LOG.isDebugEnabled()) {
            LOG.debug(report.toString());
          }
          serviceRegistry.get(BitbucketService.class).send(report);
          return report;
        }).collect(Collectors.toList());

    serviceRegistry.get(BuildBreakerService.class).failOnQualityGateIfRequested(reports);
  }
}

package de.kekru.codeanalysisbb;

import de.kekru.codeanalysisbb.bitbucket.BitbucketService;
import de.kekru.codeanalysisbb.config.Config;
import de.kekru.codeanalysisbb.config.interf.ReporterConfig;
import de.kekru.codeanalysisbb.reporter.interf.Reporter;
import de.kekru.codeanalysisbb.serviceregistry.ServiceRegistry;
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

    serviceRegistry.get(Config.class)
        .getReporter()
        .getActiveReporters()
        .stream()
        .map(ReporterConfig::getReporterService)
        .map(serviceRegistry::get)
        .map(Reporter::getBitbucketReport)
        .forEach(report -> {
          if (LOG.isDebugEnabled()) {
            LOG.debug(report.toString());
          }
         serviceRegistry.get(BitbucketService.class).send(report);
        });
  }
}

package de.kekru.nociplugin;

import de.kekru.nociplugin.bitbucket.BitbucketService;
import de.kekru.nociplugin.config.Config;
import de.kekru.nociplugin.config.interf.ReporterConfig;
import de.kekru.nociplugin.reporter.interf.Reporter;
import de.kekru.nociplugin.serviceregistry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    new Main(new ServiceRegistry());
  }

  public Main(ServiceRegistry serviceRegistry) {

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

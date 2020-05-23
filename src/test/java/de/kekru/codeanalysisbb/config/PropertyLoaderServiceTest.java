package de.kekru.codeanalysisbb.config;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class PropertyLoaderServiceTest {

  PropertyLoaderService propertyLoaderService = new PropertyLoaderService();

  @Test
  public void testPropertyLoader() {

    // Given
    Map<String, String> props = new HashMap<>();
    props.put("codeanalysisbb.bitbucket.endPoint", "https://bitbucket.example.com");
    props.put("codeanalysisbb.bitbucket.token", "Sometoken");
    props.put("codeanalysisbb.bitbucket.project", "some-project");
    props.put("codeanalysisbb.bitbucket.repo", "some-repo");

    props.put("codeanalysisbb.reporter.pmd.inputXmls",
        "build/reports/pmd/main.xml, build/reports/pmd/test.xml");
    props.put("codeanalysisbb.reporter.pmd.enabled", "true");
    props.put("codeanalysisbb.reporter.pmd.key", "pmd-analysis");
    props.put("codeanalysisbb.reporter.pmd.title", "PMD Code Analysis Report");
    props.put("codeanalysisbb.reporter.pmd.reporter", "PMD");

    props.put("codeanalysisbb.reporter.spotbugs.inputXmls", "build/reports/spotbugs/main.xml");
    props.put("codeanalysisbb.reporter.spotbugs.enabled", "false");
    props.put("codeanalysisbb.reporter.spotbugs.key", "spotbugs-key");
    props.put("codeanalysisbb.reporter.spotbugs.title", "Spotbugs Code Analysis Report");
    props.put("codeanalysisbb.reporter.spotbugs.reporter", "Spotbugs");

    Config config = new Config();

    // When
    propertyLoaderService.applyConfigProperties(config, props, ".", "codeanalysisbb");

    // Then
    assertEquals("https://bitbucket.example.com", config.getBitbucket().getEndPoint());
    assertEquals("Sometoken", config.getBitbucket().getToken());
    assertEquals("some-project", config.getBitbucket().getProject());
    assertEquals("some-repo", config.getBitbucket().getRepo());

    List<String> inputXmlsPmd = config.getReporter().getPmd().getInputXmls();
    assertEquals("build/reports/pmd/main.xml", inputXmlsPmd.get(0));
    assertEquals("build/reports/pmd/test.xml", inputXmlsPmd.get(1));
    assertEquals(2, inputXmlsPmd.size());
    assertEquals(true, config.getReporter().getPmd().isEnabled());
    assertEquals("pmd-analysis", config.getReporter().getPmd().getKey());
    assertEquals("PMD Code Analysis Report", config.getReporter().getPmd().getTitle());
    assertEquals("PMD", config.getReporter().getPmd().getReporter());

    List<String> inputXmlsSpotbugs = config.getReporter().getSpotbugs().getInputXmls();
    assertEquals("build/reports/spotbugs/main.xml", inputXmlsSpotbugs.get(0));
    assertEquals(1, inputXmlsSpotbugs.size());
    assertEquals(false, config.getReporter().getSpotbugs().isEnabled());
    assertEquals("spotbugs-key", config.getReporter().getSpotbugs().getKey());
    assertEquals("Spotbugs Code Analysis Report", config.getReporter().getSpotbugs().getTitle());
    assertEquals("Spotbugs", config.getReporter().getSpotbugs().getReporter());
  }
}

package de.kekru.codeanalysisbb.config;

import de.kekru.codeanalysisbb.shell.ShellExecutorService;
import de.kekru.javautils.config.PropertyLoaderService;
import de.kekru.javautils.dependencyinjection.ServiceProvider;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@RequiredArgsConstructor
public class ConfigProvider implements ServiceProvider<Config> {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigProvider.class);
  private Config config;
  private final ShellExecutorService shellExecutorService;
  private final PropertyLoaderService propertyLoaderService;

  private static final String PROPERTY_PREFIX = "codeanalysisbb";
  private static final String CONFIG_FILE_PROPERTY = "codeAnalysisBBConfigFile";

  @Override
  public Config getService() {
    if (config == null) {
      config = readFromYaml();
      overrideWithEnvVars(config);
      overrideWithSystemProperties(config);

      config.setWorkDir(removeLastSlash(config.getWorkDir()));

      if (StringUtils.isBlank(config.getWorkDir())) {
        config.setWorkDir(Paths.get(".").toAbsolutePath().normalize().toString());
      }
      config.getBitbucket().setCommitId(getCommitId());
    }

    return config;
  }

  private String removeLastSlash(final String path) {
    final String pathTrimmed = StringUtils.trimToEmpty(path);

    if (StringUtils.isBlank(path)) {
      return pathTrimmed;
    }

    if (StringUtils.endsWith(path, "/")) {
      return StringUtils.removeEnd(path, "/");
    }

    if (StringUtils.endsWith(path, "\\")) {
      return StringUtils.removeEnd(path, "\\");
    }

    return path;
  }

  private String getCommitId() {
    String commitId = config.getBitbucket().getCommitId();

    if (StringUtils.isBlank(commitId)) {
      commitId = shellExecutorService.execute(Arrays.asList("git", "rev-parse", "HEAD"));
      config.getBitbucket().setCommitId(commitId);
    }

    return commitId;
  }

  private void overrideWithEnvVars(Config config) {
    propertyLoaderService.applyConfigProperties(
        config,
        System.getenv(),
        "_",
        PROPERTY_PREFIX
    );
  }

  private void overrideWithSystemProperties(Config config) {
    propertyLoaderService.applyConfigProperties(
        config,
        new <String, String>HashMap(System.getProperties()),
        ".",
        PROPERTY_PREFIX
    );
  }

  private Config readFromYaml() {

    String configFile = System.getProperty(CONFIG_FILE_PROPERTY);
    if (StringUtils.isBlank(configFile)) {
      configFile = System.getenv(CONFIG_FILE_PROPERTY);
    }
    if (StringUtils.isBlank(configFile)) {
      configFile = "code-analysis-bb.yml";
    }

    File yamlConfigFile = new File(configFile).getAbsoluteFile();
    LOG.debug("Using config file: " + yamlConfigFile);

    if (!yamlConfigFile.exists()) {
      throw new RuntimeException("Config file not found under: " + yamlConfigFile +
          ", define another location by setting Java property (-D) or env var: " + CONFIG_FILE_PROPERTY
          +
          "=some/other/dir/code-analysis-bb.yml");
    }

    try {
      Yaml yaml = new Yaml(new Constructor(Config.class));
      return yaml.load(new FileInputStream(yamlConfigFile));
    } catch (Exception e) {
      throw new RuntimeException("Could not read Yaml File " + yamlConfigFile.getAbsolutePath(), e);
    }
  }
}

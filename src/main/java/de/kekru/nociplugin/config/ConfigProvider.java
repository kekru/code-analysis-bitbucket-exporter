package de.kekru.nociplugin.config;

import de.kekru.nociplugin.serviceregistry.ServiceProvider;
import de.kekru.nociplugin.shell.ShellExecutorService;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@RequiredArgsConstructor
public class ConfigProvider implements ServiceProvider<Config> {

  private Config config;
  private final ShellExecutorService shellExecutorService;

  @Override
  public Config getService() {
    if (config == null) {
      config = readFromYaml();

      if (config.getWorkDir() == null) {
        config.setWorkDir(Paths.get(".").toAbsolutePath().normalize());
      }

      config.getBitbucket().setCommitId(getCommitId());
    }

    return config;
  }

  private String getCommitId() {
    String commitId = config.getBitbucket().getCommitId();

    if (StringUtils.isBlank(commitId)) {
      commitId = shellExecutorService.execute(Arrays.asList("git", "rev-parse", "HEAD"));
      config.getBitbucket().setCommitId(commitId);
    }

    return commitId;
  }

  private Config readFromYaml() {

    String configFile = System.getProperty("configFile");
    if (configFile == null) {
      configFile = "no-ci-plugin.yml";
    }

    File yamlConfigFile = new File(configFile).getAbsoluteFile();

    try {
      Yaml yaml = new Yaml(new Constructor(Config.class));
      return yaml.load(new FileInputStream(yamlConfigFile));
    } catch (Exception e) {
      throw new RuntimeException("Could not read Yaml File " + yamlConfigFile.getAbsolutePath(), e);
    }
  }
}

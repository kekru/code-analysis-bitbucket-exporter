package de.kekru.codeanalysisbb.utils;

import de.kekru.codeanalysisbb.bitbucket.BitbucketService;
import de.kekru.codeanalysisbb.config.Config;
import de.kekru.javautils.dependencyinjection.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class FileService {

  private static final Logger LOG = LoggerFactory.getLogger(BitbucketService.class);
  private static final String PROPERTY_FILE_DELIMITER = "=";

  private final Config config;

  public String relativizeAndCleanupPath(final String target, List<String> stripPathPrefixes) {
    String modifiedTarget = replaceBackslashesAndTrim(target);

    for (final String prefix : stripPathPrefixes) {
      final String prefixModified = replaceBackslashesAndTrim(prefix);

      if (StringUtils.startsWith(modifiedTarget, prefixModified)) {
        modifiedTarget = StringUtils.removeStart(modifiedTarget, prefixModified);
      }
    }

    final String result = StringUtils.removeStart(modifiedTarget, "/");
    LOG.debug(String
        .format("Removing %s from path '%s'. Result: '%s'", stripPathPrefixes, target, result));
    return result;
  }

  private String replaceBackslashesAndTrim(String relativeFileName) {
    return StringUtils.replace(StringUtils.trimToEmpty(relativeFileName), "\\", "/");
  }

  public static Map<String, String> readFromFileToMap(final String filename) {
    final List<String> lines = readFromFile(filename);
    final Map<String, String> result = new HashMap<>();

    for (final String line : lines) {
      if (line.contains(PROPERTY_FILE_DELIMITER)) {

        final int indexDelimiter = line.indexOf(PROPERTY_FILE_DELIMITER);
        final String key = line.substring(0, indexDelimiter);
        final String value = line.substring(indexDelimiter + PROPERTY_FILE_DELIMITER.length());
        result.put(key, value);

      } else {
        LOG.debug("Could not parse line to Java Map: " + line);
      }
    }

    return result;
  }

  public static List<String> readFromFile(final String filename) {
    try (final BufferedReader reader = new BufferedReader(new FileReader(new File(filename)))) {
      return reader.lines().collect(Collectors.toList());

    } catch (IOException e) {
      throw new RuntimeException("Failed to read file: " + filename, e);
    }
  }


  public String toAbsoluteFilename(final String filename) {

    String absolutePath = filename;

    if (!Paths.get(absolutePath).isAbsolute()) {
      absolutePath = config.getWorkDir() + "/" + filename;
    }

    if (!Files.exists(Paths.get(absolutePath))) {
      throw new CodeAnalysisBitbucketException("File not found: " + absolutePath);
    }

    return absolutePath;
  }
}

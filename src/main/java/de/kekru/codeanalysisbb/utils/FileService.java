package de.kekru.codeanalysisbb.utils;

import de.kekru.codeanalysisbb.bitbucket.BitbucketService;
import de.kekru.javautils.dependencyinjection.Service;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileService {

  private static final Logger LOG = LoggerFactory.getLogger(BitbucketService.class);

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
}

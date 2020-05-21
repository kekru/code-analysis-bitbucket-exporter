package de.kekru.codeanalysisbb.utils;

import de.kekru.codeanalysisbb.serviceregistry.Service;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

@Service
public class FileService {

  public String relativizeAndCleanupPath(String target, List<String> stripPathPrefixes) {
    String modifiedTarget = replaceBackslashesAndTrim(target);

    for (String prefix : stripPathPrefixes) {
      String prefixModified = replaceBackslashesAndTrim(prefix);

      if (StringUtils.startsWith(modifiedTarget, prefixModified)) {
        modifiedTarget = StringUtils.removeStart(modifiedTarget, prefixModified);
      }
    }

    return StringUtils.removeStart(modifiedTarget, "/");
  }

  private String replaceBackslashesAndTrim(String relativeFileName) {
    return StringUtils.replace(StringUtils.trimToEmpty(relativeFileName), "\\", "/");
  }
}

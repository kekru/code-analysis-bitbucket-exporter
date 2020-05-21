package de.kekru.codeanalysisbb.testutils;

import de.kekru.codeanalysisbb.serviceregistry.ServiceRegistry;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestServiceRegistry extends ServiceRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(TestServiceRegistry.class);
  private transient Map<Class<?>, Object> serviceOverrides = new HashMap<>();

  @Override
  public <T> T get(Class<T> type) {
    if (serviceOverrides.containsKey(type)) {
      LOG.debug("Return mocked service: " + type);
      return (T) serviceOverrides.get(type);
    }

    return super.get(type);
  }

  public <T> void overrideService(Class<T> serviceClass, T implementationOrMock) {
    serviceOverrides.put(serviceClass, implementationOrMock);
  }

}

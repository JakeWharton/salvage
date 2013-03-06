package com.jakewharton.salvage;

import java.lang.reflect.Method;
import org.junit.runners.model.InitializationError;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowApplication;

public class SalvageTestRunner extends RobolectricTestRunner {
  public SalvageTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override public void setupApplicationState(Method testMethod) {
    Robolectric.application = ShadowApplication.bind(createApplication(), null, null);
  }
}

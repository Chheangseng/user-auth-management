package com.tcs.user_auth_management.config.graalVM_hint;

import com.maxmind.db.Metadata;
import com.maxmind.db.Network;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class ResourceHints implements RuntimeHintsRegistrar {

  @Override
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    // This tells GraalVM to include everything in the rsa folder
    hints.resources().registerPattern("rsa/*.pem");
    hints.resources().registerPattern("ip-location/*.mmdb");

    // 2. MaxMind Reflection Hints (The Fix)
    // We must tell GraalVM to keep constructors and methods for these classes
    hints.reflection().registerType(Metadata.class,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.PUBLIC_FIELDS);

    // Sometimes the Network class also needs registration
    hints.reflection().registerType(Network.class,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
    hints.proxies().registerJdkProxy(HttpServletRequest.class);
  }
}

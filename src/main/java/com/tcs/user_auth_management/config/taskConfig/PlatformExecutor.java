package com.tcs.user_auth_management.config.taskConfig;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class PlatformExecutor implements TaskExecutor, Executor {
  private final Executor executor;

  public PlatformExecutor(@Qualifier("platformTaskExecutor") Executor executor) {
    this.executor = executor;
  }

  @Override
  public void execute(Runnable task) {
    executor.execute(task);
  }
}

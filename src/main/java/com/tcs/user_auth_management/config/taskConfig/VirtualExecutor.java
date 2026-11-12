package com.tcs.user_auth_management.config.taskConfig;

import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VirtualExecutor implements TaskExecutor, Executor {
  private final SimpleAsyncTaskExecutor executor;

  public VirtualExecutor() {
    this.executor = new SimpleAsyncTaskExecutor();
    this.executor.setVirtualThreads(true);
    this.executor.setThreadNamePrefix("Virtual-");
  }

  @Override
  public void execute(Runnable task) {
    this.executor.execute(() -> {
      try {
        task.run();
      } catch (Throwable t) {
        log.error("Uncaught exception in virtual thread task: {}", t.getMessage(), t);
        throw t;
      }
    });
  }
}

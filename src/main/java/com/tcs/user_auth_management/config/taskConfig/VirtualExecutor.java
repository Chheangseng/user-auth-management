package com.tcs.user_auth_management.config.taskConfig;

import java.util.concurrent.Executor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class VirtualExecutor implements TaskExecutor, Executor {
  private final SimpleAsyncTaskExecutor executor;

  public VirtualExecutor() {
    this.executor = new SimpleAsyncTaskExecutor();
    this.executor.setVirtualThreads(true);
    this.executor.setThreadNamePrefix("Virtual-");
  }

  @Override
  public void execute(Runnable task) {
    this.executor.execute(task);
  }
}

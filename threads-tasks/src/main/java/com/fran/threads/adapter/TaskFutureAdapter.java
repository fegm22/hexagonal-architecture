package com.fran.threads.adapter;

import com.fran.task.domain.model.Task;
import com.fran.task.domain.port.TaskManager;
import com.fran.threads.exception.CounterTaskNotFoundException;
import com.fran.threads.model.TaskFuture;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskFutureAdapter implements TaskManager {

    private final Map<String, TaskFuture> taskRegister;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutorService;

    @SneakyThrows
    public void cancelTask(String taskId) {
        TaskFuture taskThread = taskRegister.get(taskId);
        if (taskThread.future() != null) {
            taskThread.future().cancel(true);
        }
        taskRegister.remove(taskId);
    }

    public Task executeTask(Task task) {
        if (task == null) {
            throw new CounterTaskNotFoundException("Failed to find counter with ID ");
        }

        Future<Task> progressFuture = executorService.submit(() -> {
            for (int i = task.getBegin(); i <= task.getFinish(); i++) {
                task.setProgress(i);
                log.info("Counter progress is '{}' for '{}' running in '{}'", task.getProgress(), task.getId(), Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return task;
        });

        taskRegister.put(task.getId(), new TaskFuture(task, progressFuture));

        return task;
    }

    public List<Task> getAllRunningCounters() {
        removeFinishedTasks();
        return taskRegister.values().stream()
                .map(TaskFuture::task)
                .filter(task -> task.getFinish() > task.getProgress())
                .toList();
    }

    public Task getRunningCounter(String counterId) {
        if (taskRegister.isEmpty() || taskRegister.get(counterId) == null) {
            log.error("Failed to find counter with ID " + counterId);
            throw new CounterTaskNotFoundException("Failed to find counter with ID " + counterId);
        }
        removeFinishedTasks();
        TaskFuture taskThread = taskRegister.get(counterId);
        log.info("Counter progress is '{}' for '{}' running in '{}' ", taskThread.task().getProgress(), taskThread.task().getId(), taskThread.future());
        return taskThread.task();
    }

    public Flux<Task> startReceivingMessages() {
        return null;
    }

    @PostConstruct
    private void createScheduleRemoveTask() {
        Runnable deleteTasksSchedule = () -> {
            if (taskRegister != null && !taskRegister.isEmpty()) {
                log.info("Schedule executing every 5 minutes will remove {} tasks from the Running Register.", taskRegister.size());
                taskRegister.clear();
            } else {
                log.info("Not running tasks will be deleted from Running Register");
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(deleteTasksSchedule, 0, 5, TimeUnit.MINUTES);
    }

    private void removeFinishedTasks() {
        taskRegister.values().stream()
                .map(TaskFuture::task)
                .filter(task -> Objects.equals(task.getFinish(), task.getProgress()))
                .forEach(task -> taskRegister.remove(task.getId()));
    }

}

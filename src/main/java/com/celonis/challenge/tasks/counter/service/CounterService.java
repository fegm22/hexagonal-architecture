package com.celonis.challenge.tasks.counter.service;

import com.celonis.challenge.tasks.counter.jobs.CounterJob;
import com.celonis.challenge.tasks.counter.model.Counter;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CounterService {

    private final SchedulerService scheduler;

    public void runCounterJob(Counter task) throws SchedulerException {
        task.setProgress(task.getBegin()-1);
        scheduler.schedule(CounterJob.class, task);
    }

    public Boolean cancelCounter(final String counterId) {
        return scheduler.cancelCounter(counterId);
    }

    public List<Counter> getAllRunningCounters() {
        return scheduler.getAllRunningCounters();
    }

    public Counter getRunningCounter(final String counterId) throws SchedulerException {
        return scheduler.getRunningCounter(counterId);
    }

}

package com.fran.task.flux.handle;

import com.fran.task.flux.dto.ProjectGenerationTask;
import com.fran.task.flux.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.reactive.function.server.ServerResponse.*;
import static reactor.core.publisher.Mono.defer;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskHandler {

    private final TaskService service;

    public Mono<ServerResponse> createTask(final ServerRequest request) {
        return ok()
                .contentType(APPLICATION_JSON)
                .body(request
                        .bodyToMono(ProjectGenerationTask.class)
                        .map(service::createTask)
                        .flatMap(Mono::just), ProjectGenerationTask.class);
    }

    public Mono<ServerResponse> listTasks(final ServerRequest request) {
        return ok()
                .contentType(APPLICATION_JSON)
                .body(Flux.fromIterable(service.listTasks()), ProjectGenerationTask.class);

    }

    public Mono<ServerResponse> getTask(final ServerRequest request) {
        return Mono
                .just(request.pathVariable("taskId"))
                .map(service::getTask)
                .flatMap(generationTask -> ok()
                        .contentType(APPLICATION_JSON)
                        .body(Mono.just(generationTask), ProjectGenerationTask.class))
                .onErrorResume(error -> Mono.just(error)
                        .flatMap(response -> notFound().build()))
                .switchIfEmpty(defer(() -> notFound()
                        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .build()));
    }

    public Mono<ServerResponse> updateTask(final ServerRequest request) {
        String taskId = request.pathVariable("taskId");
        return ok()
                .contentType(APPLICATION_JSON)
                .body(request
                        .bodyToMono(ProjectGenerationTask.class)
                        .map(generationTask -> service.updateTask(taskId, generationTask))
                        .flatMap(Mono::just), ProjectGenerationTask.class);
    }

    public Mono<ServerResponse> deleteTask(final ServerRequest request) {
        return Mono
                .just(request.pathVariable("taskId"))
                .map(service::deleteTask)
                .flatMap(resource -> noContent().build())
                .switchIfEmpty(defer(() -> notFound()
                        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .build()));
    }

    public Mono<ServerResponse> executeTask(final ServerRequest request) {
        return Mono
                .just(request.pathVariable("taskId"))
                .map(service::executeTask)
                .flatMap(resource -> noContent().build())
                .switchIfEmpty(defer(() -> notFound()
                        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .build()));
    }

    public Mono<ServerResponse> cancelTask(final ServerRequest request) {
        return Mono
                .just(request.pathVariable("taskId"))
                .map(service::cancelTask)
                .flatMap(resource -> noContent().build())
                .switchIfEmpty(defer(() -> notFound()
                        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .build()));
    }

    public Mono<ServerResponse> getAllRunningCounters(final ServerRequest request) {
        return ok()
                .contentType(APPLICATION_JSON)
                .body(Flux.fromIterable(service.getAllRunningCounters()), ProjectGenerationTask.class);

    }

    public Mono<ServerResponse> getRunningCounter(final ServerRequest request) {
        return Mono
                .just(request.pathVariable("taskId"))
                .map(service::getRunningCounter)
                .flatMap(generationTask -> ok()
                        .contentType(APPLICATION_JSON)
                        .body(Mono.just(generationTask), ProjectGenerationTask.class))
                .onErrorResume(error -> Mono.just(error.getMessage())
                        .flatMap(response -> notFound().build()))
                .switchIfEmpty(defer(() -> notFound()
                        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .build()));
    }

}

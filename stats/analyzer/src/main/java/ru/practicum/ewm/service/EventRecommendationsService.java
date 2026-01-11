package ru.practicum.ewm.service;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.grpc.stats.controller.RecommendationsControllerGrpc;
import ru.practicum.ewm.grpc.stats.event.InteractionsCountRequestProto;
import ru.practicum.ewm.grpc.stats.event.RecommendedEventProto;
import ru.practicum.ewm.grpc.stats.event.SimilarEventsRequestProto;
import ru.practicum.ewm.grpc.stats.event.UserPredictionsRequestProto;
import ru.practicum.ewm.handler.RecommendationsHandler;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class EventRecommendationsService extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationsHandler handler;

    @Override
    public void getRecommendationsUser(UserPredictionsRequestProto request,
                                       StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Получение рекомендаций для пользователя {}", request);

        try {
            handler.getRecommendationsUser(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Получение похожих событий {}", request);

        try {
            handler.getSimilarEvents(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        log.info("Количества взаимодействий с мероприятиями {}", request);

        try {
            handler.getInteractionCount(request).forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}

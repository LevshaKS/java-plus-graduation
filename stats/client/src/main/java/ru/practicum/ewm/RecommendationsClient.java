package ru.practicum.ewm;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.grpc.stats.controller.RecommendationsControllerGrpc;
import ru.practicum.ewm.grpc.stats.event.InteractionsCountRequestProto;
import ru.practicum.ewm.grpc.stats.event.RecommendedEventProto;
import ru.practicum.ewm.grpc.stats.event.SimilarEventsRequestProto;
import ru.practicum.ewm.grpc.stats.event.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class RecommendationsClient {
    private final RecommendationsControllerGrpc.RecommendationsControllerBlockingStub recommendationsStub;

    public RecommendationsClient(
            @GrpcClient("analyzer") RecommendationsControllerGrpc.RecommendationsControllerBlockingStub recommendationsStub) {
        this.recommendationsStub = recommendationsStub;
    }

    public Stream<RecommendedEventProto> getRecommendationsUser(long userId, int maxResults) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        Iterator<RecommendedEventProto> iterator = recommendationsStub.getRecommendationsUser(request);

        return streams(iterator);
    }

    public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        Iterator<RecommendedEventProto> iterator = recommendationsStub.getSimilarEvents(request);

        return streams(iterator);
    }

    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();

        Iterator<RecommendedEventProto> iterator = recommendationsStub.getInteractionsCount(request);

        return streams(iterator);
    }

    private Stream<RecommendedEventProto> streams(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}


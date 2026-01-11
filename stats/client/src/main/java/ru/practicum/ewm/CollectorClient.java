package ru.practicum.ewm;


import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.grpc.stats.controller.UserActionControllerGrpc;
import ru.practicum.ewm.grpc.stats.event.ActionTypeProto;
import ru.practicum.ewm.grpc.stats.event.UserActionProto;

import java.time.Instant;


@Service
public class CollectorClient {
    private final UserActionControllerGrpc.UserActionControllerBlockingStub userActionStub;


    public CollectorClient(
            @GrpcClient("collector") UserActionControllerGrpc.UserActionControllerBlockingStub userActionStub) {
        this.userActionStub = userActionStub;
    }

    public void collectUserAction(Long eventId, Long userId, ActionTypeProto type, Instant instant) {
        UserActionProto request = UserActionProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setActionType(type)
                .setTimestamp(toTimestamp(instant))
                .build();

        userActionStub.collectUserAction(request);
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

}

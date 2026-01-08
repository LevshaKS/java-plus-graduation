package ru.practicum.ewm.handler;

import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.ewm.grpc.stats.event.UserActionProto;

public interface UserActionHandler {

    void handle(UserActionProto userActionProto);

    SpecificRecordBase toAvro(UserActionProto userActionProto);
}

package ru.practicum.ewm.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.grpc.stats.controller.UserActionControllerGrpc;

import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.grpc.stats.event.UserActionProto;
import ru.practicum.ewm.handler.UserActionHandler;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final UserActionHandler userActionHandler;


    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {  //request - событие от датчика   //responceObserver - ответ для клиента
        log.info("получаем сообщение: {}", request);
        try { //проверяем если ли обраотчик для полученого события
            userActionHandler.handle(request);
            responseObserver.onNext(Empty.getDefaultInstance()); //после обраотки события возвращаем ответ клиенту
            responseObserver.onCompleted();  //завершаем обработку запроса
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(Status.fromThrowable(e)));  //в случаи ошибки отправляем ошибку клиенту
        }
    }

}

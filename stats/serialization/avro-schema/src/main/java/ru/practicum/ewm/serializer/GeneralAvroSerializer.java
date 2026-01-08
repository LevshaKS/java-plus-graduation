package ru.practicum.ewm.serializer;


import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
public class GeneralAvroSerializer implements Serializer<SpecificRecordBase> {
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private BinaryEncoder encoder;


    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            log.info("сериализация данных не удачно topic: " + topic);
            return null;
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] result = null;
            encoder = encoderFactory.binaryEncoder(outputStream, encoder);

            DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
            writer.write(data, encoder);
            encoder.flush();

            result = outputStream.toByteArray();
            log.info("сериализация данных {} topic: {} ", data, topic);
            return result;
        } catch (IOException e) {
            throw new SerializationException("Ошибка сериализации данных topic: " + topic);
        }
    }
}
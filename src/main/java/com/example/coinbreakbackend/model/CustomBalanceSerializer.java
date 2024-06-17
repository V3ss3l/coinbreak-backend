package com.example.coinbreakbackend.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class CustomBalanceSerializer extends StdSerializer<BalanceDto> {

    public CustomBalanceSerializer() {
        this(null);
    }

    public CustomBalanceSerializer(Class<BalanceDto> t) {
        super(t);
    }

    @Override
    public void serialize(BalanceDto balanceDto, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("wallet", balanceDto.getWallet());
        jsonGenerator.writeNumberField("amount", balanceDto.getAmount());
        jsonGenerator.writeObjectField("currency", balanceDto.getCurrency());
        jsonGenerator.writeEndObject();
    }
}

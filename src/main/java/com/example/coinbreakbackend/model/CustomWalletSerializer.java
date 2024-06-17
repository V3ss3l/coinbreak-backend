package com.example.coinbreakbackend.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class CustomWalletSerializer extends StdSerializer<WalletDto> {

    public CustomWalletSerializer(){
        this(null);
    }

    public CustomWalletSerializer(Class<WalletDto> t) {
        super(t);
    }

    @Override
    public void serialize(WalletDto walletDto, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", walletDto.getId());
        jsonGenerator.writeStringField("public_key", walletDto.getPublicKey());
        jsonGenerator.writeStringField("seed", walletDto.getSeed());
        jsonGenerator.writeEndObject();
    }
}

package com.example.coinbreakbackend.config;

import com.example.coinbreakbackend.model.BalanceDto;
import com.example.coinbreakbackend.model.CustomBalanceSerializer;
import com.example.coinbreakbackend.model.CustomWalletSerializer;
import com.example.coinbreakbackend.model.WalletDto;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MapperConfig {
    @Bean
    public ObjectMapper initMapper(){
       ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module =
                new SimpleModule("CustomWalletSerializer", new Version(1, 0, 0, null, null, null));
        module.addSerializer(WalletDto.class, new CustomWalletSerializer(WalletDto.class));
        objectMapper.registerModule(module);

        SimpleModule module_1 =
                new SimpleModule("CustomBalanceSerializer", new Version(1, 0, 0, null, null, null));
        module_1.addSerializer(BalanceDto.class, new CustomBalanceSerializer(BalanceDto.class));
        objectMapper.registerModule(module_1);
        return objectMapper;
    }
}

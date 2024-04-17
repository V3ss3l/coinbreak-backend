package com.example.coinbreakbackend.controller;

import ch.qos.logback.core.util.ContentTypeUtil;
import com.example.coinbreakbackend.model.WalletDto;
import com.example.coinbreakbackend.service.WalletService;
import com.example.coinbreakbackend.service.impl.WalletServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    private final WalletService walletService;
    private ObjectMapper objectMapper;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping(path = "/balance/currency/{symbol}")
    public ResponseEntity<Long> getBalanceByCurrency(@PathVariable String symbol){
        return new ResponseEntity<>(1L, HttpStatusCode.valueOf(200));
    }

    @PostMapping(path = "/deposit")
    public ResponseEntity<Long> depositToWallet(){
        return new ResponseEntity<>(1L, HttpStatusCode.valueOf(200));
    }

    @PostMapping(path = "/withdraw")
    public ResponseEntity<Long> withdrawToWallet(@PathVariable String adressantKey){
        return new ResponseEntity<>(1L, HttpStatusCode.valueOf(200));
    }

    @GetMapping(path = "/create")
    public ResponseEntity<WalletDto> createWallet(@RequestParam String password){
        WalletDto dto = walletService.generate(password);
        if (Objects.nonNull(dto)) return ResponseEntity.ok().body(dto);
        else ResponseEntity.internalServerError();
        return null;
    }

}
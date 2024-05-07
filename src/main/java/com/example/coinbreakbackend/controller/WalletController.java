package com.example.coinbreakbackend.controller;

import com.example.coinbreakbackend.model.ResponseInfo;
import com.example.coinbreakbackend.service.WalletService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/wallet")
@CrossOrigin(origins = "http://localhost:4200")
public class WalletController {
    private final WalletService walletService;
    private ObjectMapper objectMapper;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping(path = "/balance/currency/{currency}", produces = "application/json")
    public ResponseEntity<ResponseInfo> getBalanceByCurrency(@PathVariable String currency) throws JsonProcessingException {
        var result = walletService.balance(currency);
        var response = ResponseInfo.builder()
                .data(result)
                .build();
        if(((Map<?, ?>) result).containsKey("stackTrace")){
            response.setHttpCode(HttpStatusCode.valueOf(500).toString());
            response.setStackTrace((String) ((Map<?, ?>) result).get("stackTrace"));
            response.setInfo("При получении баланса возникла ошибка");
        }
        else {
            response.setHttpCode(HttpStatusCode.valueOf(200).toString());
            response.setInfo(String.format("Баланс кошелька по валюте %s отправлен", currency));
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/deposit", produces = "application/json")
    public ResponseEntity<ResponseInfo> depositToWallet(@RequestParam Map<String, Object> params){
        Long amount = Long.valueOf((String) params.get("amount"));
        String currency = (String) params.get("currency");
        var result = walletService.deposit(amount, currency);
        var response = ResponseInfo.builder()
                .data(result)
                .build();
        if(((Map<?, ?>) result).containsKey("stackTrace")){
            response.setHttpCode(HttpStatusCode.valueOf(500).toString());
            response.setStackTrace((String) ((Map<?, ?>) result).get("stackTrace"));
            response.setInfo("При пополнении кошелька возникла ошибка");
        }
        else {
            response.setHttpCode(HttpStatusCode.valueOf(200).toString());
            response.setInfo("Кошелек был успешно пополнен");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/withdraw", produces = "application/json")
    public ResponseEntity<ResponseInfo> withdrawToWallet(@RequestParam Map<String, Object> params){
        Long amount = Long.valueOf((String) params.get("amount"));
        String currency = (String) params.get("currency");
        String to = (String) params.get("addressTo");
        var result = walletService.withdraw(amount, currency, to);
        var response = ResponseInfo.builder()
                .data(result)
                .build();
        if(((Map<?, ?>) result).containsKey("stackTrace")){
            response.setHttpCode(HttpStatusCode.valueOf(500).toString());
            response.setStackTrace((String) ((Map<?, ?>) result).get("stackTrace"));
            response.setInfo("При переводе возникла ошибка");
        }
        else {
            response.setHttpCode(HttpStatusCode.valueOf(200).toString());
            response.setInfo("Перевод был успешно завершен");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/create", produces = "application/json")
    public ResponseEntity<ResponseInfo> createWallet(@RequestParam String password){
        Object result = walletService.generate(password);
        var response = ResponseInfo.builder()
                .data(result)
                .build();
        if(((Map<?, ?>) result).containsKey("stackTrace")){
            response.setHttpCode(HttpStatusCode.valueOf(500).toString());
            response.setStackTrace((String) ((Map<?, ?>) result).get("stackTrace"));
            response.setInfo("При создании кошелка возникла ошибка");
        }
        else {
            response.setHttpCode(HttpStatusCode.valueOf(200).toString());
            response.setInfo("Кошелек был создан");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping(path="/restore", produces = "application/json")
    public ResponseEntity<ResponseInfo> restoreWallet(@RequestParam String password){
        var result = walletService.restore(password);
        ResponseInfo response = ResponseInfo.builder()
                .data(result)
                .build();
        if(((Map<?, ?>) result).containsKey("stackTrace")){
            response.setInfo("Произошла ошибка при восстановлении кошелька");
            response.setHttpCode(HttpStatusCode.valueOf(500).toString());
            response.setStackTrace((String) ((Map<?, ?>) result).get("stackTrace"));
        } else {
            response.setHttpCode(HttpStatusCode.valueOf(200).toString());
            response.setInfo("Восстановлен доступ к кошельку");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(path="/generate/seed", produces = "application/json")
    @ResponseBody
    public ResponseEntity<ResponseInfo> generateSeed(@RequestParam Map<String, Object> params){
        var sizeOfSeed = Integer.valueOf((String) params.get("size"));
        var language = (String) params.get("language");
        Object result = walletService.generateSeed(sizeOfSeed, language);
        ResponseInfo response = ResponseInfo.builder()
                .data(result)
                .build();
        if(((Map<?, ?>) result).containsKey("stackTrace")){
            response.setInfo("Произошла ошибка при генерации сид фразы");
            response.setHttpCode(HttpStatusCode.valueOf(500).toString());
            response.setStackTrace((String) ((Map<?, ?>) result).get("stackTrace"));
        } else {
            response.setHttpCode(HttpStatusCode.valueOf(200).toString());
            response.setInfo("Генерация сид фразы прошла успешно");
        }
        return ResponseEntity.ok(response);
    }

}
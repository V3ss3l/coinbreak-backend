package com.example.coinbreakbackend.controller;

import com.example.coinbreakbackend.model.ResponseInfo;
import com.example.coinbreakbackend.service.CurrencyService;
import com.example.coinbreakbackend.service.EthWalletService;
import com.example.coinbreakbackend.service.WalletService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
@CrossOrigin(origins = "http://localhost:4200")
public class WalletController {
    private final EthWalletService walletService;
    private final CurrencyService currencyService;

    public WalletController(EthWalletService walletService, CurrencyService currencyService) {
        this.walletService = walletService;
        this.currencyService = currencyService;
    }

    @GetMapping(path = "/balance/currency/{currency}", produces = "application/json")
    public ResponseEntity<ResponseInfo> getBalanceByCurrency(@PathVariable String currency) {
        var result = currencyService.balance(currency);
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

    @GetMapping(path = "/balance/currency/all", produces = "application/json")
    public ResponseEntity<ResponseInfo> getBalanceByAllCurrencies(@RequestParam Map<String, Object> params) {
        List<Map<String, Object>> result = new ArrayList<>();
        params.forEach((key, value) -> result.add((Map<String, Object>) currencyService.balanceAll((String) value)));
        ResponseInfo response = ResponseInfo.builder()
                .data(result)
                .build();

        if(result.stream().anyMatch(pr -> pr.containsKey("stackTrace"))){
            response.setHttpCode(HttpStatusCode.valueOf(500).toString());
            response.setStackTrace((String) result.stream()
                    .filter(pr -> pr.containsKey("stackTrace"))
                    .findFirst().get()
                    .get("stackTrace"));
            response.setInfo("При получении баланса возникла ошибка");
        }
        else {
            response.setHttpCode(HttpStatusCode.valueOf(200).toString());
            response.setInfo("Баланс кошелька отправлен");
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
    public ResponseEntity<ResponseInfo> createWallet(@RequestParam Map<String, Object> params){
        var password = (String) params.get("password");
        var seed = (String) params.get("seed");
        Object result = walletService.generate(password, seed);
        var response = ResponseInfo.builder()
                .data(result)
                .build();
        if(((Map<?, ?>) result).containsKey("stackTrace")){
            response.setHttpCode(HttpStatusCode.valueOf(500).toString());
            response.setStackTrace((String) ((Map<?, ?>) result).get("stackTrace"));
            response.setInfo("При создании кошелька возникла ошибка");
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
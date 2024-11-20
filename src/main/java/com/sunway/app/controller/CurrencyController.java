package com.sunway.app.controller;

import com.sunway.app.service.CurrencyService;
import com.sunway.app.model.CoindeskResponse;
import com.sunway.app.model.Currency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class CurrencyController {
    @Autowired
    private CurrencyService currencyService;

    @GetMapping("/currency")
    public List<Currency> getAllCurrencies() {
        return currencyService.getAllCurrencies();
    }

    @GetMapping("/currency/{id}")
    public Currency getCurrency(@PathVariable Long id) {
        return currencyService.getCurrencyById(id);
    }

    @PostMapping("/currency")
    public Currency createCurrency(@Valid @RequestBody Currency currency) {
        return currencyService.createCurrency(currency);
    }

    @PutMapping("/currency/{id}")
    public Currency updateCurrency(@PathVariable Long id, @Valid @RequestBody Currency currency) {
        return currencyService.updateCurrency(id, currency);
    }

    @DeleteMapping("/currency/{id}")
    public void deleteCurrency(@PathVariable Long id) {
        currencyService.deleteCurrency(id);
    }

    @GetMapping("/coindesk")
    public CoindeskResponse getCoindeskData() {
        return currencyService.getCoindeskData();
    }

    @GetMapping("/coindesk/convert")
    public Object convertCoindeskData() {
        CoindeskResponse coindeskData = currencyService.getCoindeskData();
        return currencyService.convertCoindeskData(coindeskData);
    }
}

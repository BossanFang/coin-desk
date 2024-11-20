package com.sunway.app.service;

import com.sunway.app.repository.CurrencyRepository;
import com.sunway.app.exception.CustomException;
import com.sunway.app.model.CoindeskResponse;
import com.sunway.app.model.Currency;
import com.sunway.app.model.CurrencyInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService {
    @Autowired
    private CurrencyRepository currencyRepository;

    private static final String COINDESK_API_URL = "https://api.coindesk.com/v1/bpi/currentprice.json";

    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    public Currency getCurrencyById(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(
                        () -> new CustomException(HttpStatus.NOT_FOUND.value(), "Currency not found with id: " + id));
    }

    public Currency createCurrency(Currency currency) {
        if (currencyRepository.findByCode(currency.getCode()).isPresent()) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(),
                    "Currency with code " + currency.getCode() + " already exists");
        }
        try {
            return currencyRepository.save(currency);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), "Invalid data: " + e.getMessage());
        } catch (DataAccessException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Database error: Unable to save currency");
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    public Currency updateCurrency(Long id, Currency newCurrency) {
        if (id == null || id <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), "Invalid ID");
        }

        Currency existingCurrency = currencyRepository.findById(id)
                .orElseThrow(
                        () -> new CustomException(HttpStatus.NOT_FOUND.value(), "Currency not found with id: " + id));

        if (newCurrency.getCode() == null || newCurrency.getName() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), "Currency code and name must not be null");
        }

        currencyRepository.findByCode(newCurrency.getCode())
                .filter(currency -> !currency.getId().equals(id))
                .ifPresent(conflict -> {
                    throw new CustomException(HttpStatus.BAD_REQUEST.value(),
                            "Currency code " + newCurrency.getCode() + " already exists");
                });

        existingCurrency.setCode(newCurrency.getCode());
        existingCurrency.setName(newCurrency.getName());

        try {
            return currencyRepository.save(existingCurrency);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), "Invalid data: " + e.getMessage());
        } catch (DataAccessException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Database error: Unable to save currency");
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    public void deleteCurrency(Long id) {
        if (id == null || id <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), "Invalid ID");
        }

        try {
            currencyRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new CustomException(HttpStatus.NOT_FOUND.value(), "Currency not found with id: " + id);
        } catch (DataAccessException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Database error while attempting to delete currency");
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An unexpected error occurred: " + e.getMessage());
        }
    }

    public CoindeskResponse getCoindeskData() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            return restTemplate.exchange(
                    COINDESK_API_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<CoindeskResponse>() {
                    }).getBody();
        } catch (HttpClientErrorException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(),
                    "Coindesk API returned client error: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Coindesk API returned server error");
        } catch (HttpMessageConversionException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Invalid response format from Coindesk API");
        } catch (ResourceAccessException e) {
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE.value(),
                    "Failed to connect to Coindesk API or request timed out");
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An unexpected error occurred while calling Coindesk API");
        }
    }

    public Object convertCoindeskData(CoindeskResponse coindeskData) {
        if (coindeskData == null) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Coindesk data is null");
        }
        if (coindeskData.getTime() == null || coindeskData.getTime().getUpdatedISO() == null) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "'time' or 'updatedISO' is missing in Coindesk data");
        }
        if (coindeskData.getBpi() == null) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "'bpi' field is missing in Coindesk data");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("updatedTime", formatTime(coindeskData.getTime().getUpdatedISO()));

        Map<String, Object> currencyInfo = new HashMap<>();
        for (Map.Entry<String, CurrencyInfo> entry : coindeskData.getBpi().entrySet()) {
            try {
                CurrencyInfo currencyData = entry.getValue();
                if (currencyData == null || currencyData.getRate() == null || currencyData.getCode() == null) {
                    throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Currency data for " + entry.getKey() + " is incomplete");
                }

                Map<String, Object> currencyDetails = new HashMap<>();
                currencyDetails.put("rate", currencyData.getRate());
                currencyDetails.put("currency", currencyData.getCode());
                currencyDetails.put("name", getCurrencyName(entry.getKey()));
                currencyInfo.put(entry.getKey(), currencyDetails);
            } catch (Exception e) {
                throw e;
            }
        }
        result.put("currencies", currencyInfo);

        return result;
    }

    private String formatTime(String isoTime) {
        try {
            SimpleDateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
            SimpleDateFormat targetFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return targetFormatter.format(isoFormatter.parse(isoTime));
        } catch (Exception e) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), "Failed to format time: " + isoTime);
        }
    }

    private String getCurrencyName(String currencyCode) {
        Currency currency = currencyRepository.findByCode(currencyCode).orElseThrow(
                () -> new CustomException(HttpStatus.NOT_FOUND.value(),
                        "Currency not found with code: " + currencyCode));
        return currency.getName();
    }
}

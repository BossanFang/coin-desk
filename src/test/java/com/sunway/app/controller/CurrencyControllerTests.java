package com.sunway.app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.sunway.app.model.Currency;
import com.sunway.app.repository.CurrencyRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import javax.persistence.EntityManager;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class CurrencyControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setupTestData() {
        currencyRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE currency ALTER COLUMN id RESTART WITH 1").executeUpdate();

        currencyRepository.save(new Currency("USD", "美元"));
        currencyRepository.save(new Currency("GBP", "英鎊"));
        currencyRepository.save(new Currency("EUR", "歐元"));
    }

    @Test
    void testGetCurrency() throws Exception {
        mockMvc.perform(get("/api/currency"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()));
    }

    @Test
    void testCreateCurrency() throws Exception {
        String newCurrencyJson = "{\n" +
                "    \"code\": \"JPY\",\n" +
                "    \"name\": \"日元\"\n" +
                "}";

        mockMvc.perform(post("/api/currency")
                .contentType("application/json")
                .content(newCurrencyJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("JPY"))
                .andExpect(jsonPath("$.name").value("日元"));
    }

    @Test
    void testUpdateCurrency() throws Exception {
        String updatedCurrencyJson = "{\n" +
                "    \"code\": \"JPY\",\n" +
                "    \"name\": \"日元\"\n" +
                "}";

        mockMvc.perform(put("/api/currency/{id}", 1)
                .contentType("application/json")
                .content(updatedCurrencyJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("JPY"))
                .andExpect(jsonPath("$.name").value("日元"))
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()));
    }

    @Test
    void testDeleteCurrency() throws Exception {
        mockMvc.perform(delete("/api/currency/{id}", 1))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCoindeskData() throws Exception {
        mockMvc.perform(get("/api/coindesk"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.bpi").exists())
                .andExpect(jsonPath("$.time.updatedISO").exists())
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()));
    }

    @Test
    void testConvertCoindeskData() throws Exception {
        mockMvc.perform(get("/api/coindesk/convert"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.updatedTime").exists())
                .andExpect(jsonPath("$.currencies").isMap())
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()));
    }
}

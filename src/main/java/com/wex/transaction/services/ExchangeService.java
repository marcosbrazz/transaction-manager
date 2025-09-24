package com.wex.transaction.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.wex.transaction.exceptions.InternalApplicationError;
import com.wex.transaction.exceptions.ServiceException;

@Service
public class ExchangeService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeService.class);

    private final RestClient restClient;
    private final String treasuryApiUrl;

    public ExchangeService(RestClient.Builder restClientBuilder, Environment env) {
        this.restClient = restClientBuilder.build();
        this.treasuryApiUrl = env.getProperty("treasury.api.url");
    }

    public List<ExchangeRate> getRates(String currency, LocalDate start, LocalDate end) {
        try {
            String url = String.format(
                "%s?filter=currency:eq:%s,record_date:gte:%s,record_date:lte:%s&sort=-record_date",
                treasuryApiUrl,
                currency,
                start,
                end
            );

            log.debug("Treasury API request: {}", url);

            TreasuryResponse response = restClient.get()
                .uri(url)
                .retrieve()
                .body(TreasuryResponse.class);

            if (response == null || response.data() == null) {
                throw new ServiceException("No data found for currency " + currency);
            }

            return response.data().stream()
                .map(d -> new ExchangeRate(
                    LocalDate.parse(d.get("record_date")),
                    new BigDecimal(d.get("exchange_rate"))
                ))
                .toList();
        } catch (Exception e) {
            log.error("Error on Treasury API", e);
            throw new InternalApplicationError("Error on Treasury API: " + e.getMessage());
        }
    }

    public record ExchangeRate(LocalDate recordDate, BigDecimal exchangeRate) {}

    private record TreasuryResponse(List<Map<String, String>> data) {}
}

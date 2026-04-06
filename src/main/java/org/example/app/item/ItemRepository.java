package org.example.app.item;

import java.math.BigDecimal;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface ItemRepository extends CrudRepository<Item, Long> {

    @Query("""
        SELECT
            COALESCE(AVG(price), 0.00) AS average_price,
            COALESCE(MAX(price), 0.00) AS max_price,
            COALESCE(MIN(price), 0.00) AS min_price
        FROM item
        """)
    PriceSummaryRow pricingSummary();

    record PriceSummaryRow(
        BigDecimal averagePrice,
        BigDecimal maxPrice,
        BigDecimal minPrice
    ) {
    }
}

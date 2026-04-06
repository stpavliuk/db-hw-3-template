package org.example.app.item;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("item")
public record Item(
    @Id Long id,
    String name,
    String description,
    BigDecimal price
) {
    public static Item of(String name, String description, BigDecimal price) {
        return new Item(null, name, description, price);
    }

    public Item withDetails(String name, String description, BigDecimal price) {
        return new Item(id, name, description, price);
    }
}

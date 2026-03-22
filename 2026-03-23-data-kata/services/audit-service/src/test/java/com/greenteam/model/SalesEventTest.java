package com.greenteam.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SalesEventTest {

    private SalesEvent event;

    @BeforeEach
    void setUp() {
        event = new SalesEvent();
        event.setSalesmanId(1);
        event.setSalesmanName("John Doe");
        event.setSaleId(100);
        event.setQuantity(5);
        event.setSaleDate("2024-01-15T10:30:00");
        event.setProductId(200);
        event.setStoreId(300);
        event.setCityName("New York");
        event.setStoreName("Main Store");
        event.setCountryName("USA");
        event.setAmount("1500.50");
    }

    @Test
    void toCsvRow_returnsCorrectFormat() {
        String csvRow = event.toCsvRow();

        assertEquals("1,John Doe,100,5,2024-01-15T10:30:00,200,300,New York,Main Store,USA,1500.50", csvRow);
    }

    @Test
    void toCsvRow_escapesCommasInValues() {
        event.setSalesmanName("Doe, John");

        String csvRow = event.toCsvRow();

        assertTrue(csvRow.contains("\"Doe, John\""));
    }

    @Test
    void toCsvRow_escapesQuotesInValues() {
        event.setStoreName("Store \"Best\"");

        String csvRow = event.toCsvRow();

        assertTrue(csvRow.contains("\"Store \"\"Best\"\"\""));
    }

    @Test
    void toCsvRow_escapesNewlinesInValues() {
        event.setCityName("New\nYork");

        String csvRow = event.toCsvRow();

        assertTrue(csvRow.contains("\"New\nYork\""));
    }

    @Test
    void toCsvRow_handlesNullValues() {
        event.setSalesmanName(null);
        event.setCityName(null);

        String csvRow = event.toCsvRow();

        assertNotNull(csvRow);
        assertTrue(csvRow.contains("1,,100"));
    }

    @Test
    void getCsvHeader_returnsCorrectHeader() {
        String header = SalesEvent.getCsvHeader();

        assertEquals("salesman_id,salesman_name,sale_id,quantity,sale_date,product_id,store_id,city_name,store_name,country_name,amount", header);
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        SalesEvent newEvent = new SalesEvent();

        newEvent.setSalesmanId(42);
        assertEquals(42, newEvent.getSalesmanId());

        newEvent.setSalesmanName("Jane");
        assertEquals("Jane", newEvent.getSalesmanName());

        newEvent.setSaleId(999);
        assertEquals(999, newEvent.getSaleId());

        newEvent.setQuantity(10);
        assertEquals(10, newEvent.getQuantity());

        newEvent.setSaleDate("2024-02-20");
        assertEquals("2024-02-20", newEvent.getSaleDate());

        newEvent.setProductId(555);
        assertEquals(555, newEvent.getProductId());

        newEvent.setStoreId(777);
        assertEquals(777, newEvent.getStoreId());

        newEvent.setCityName("Boston");
        assertEquals("Boston", newEvent.getCityName());

        newEvent.setStoreName("Downtown");
        assertEquals("Downtown", newEvent.getStoreName());

        newEvent.setCountryName("Canada");
        assertEquals("Canada", newEvent.getCountryName());

        newEvent.setAmount("999.99");
        assertEquals("999.99", newEvent.getAmount());
    }
}

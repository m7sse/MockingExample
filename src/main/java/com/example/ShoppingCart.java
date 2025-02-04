package com.example;

import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {

    private Map<String, Item> items;

    public ShoppingCart() {
        items = new HashMap<>();
    }

    public void addItem(String itemName, double itemPrice) {
        if (itemName == null || itemPrice < 0) {
            throw new IllegalArgumentException("Invalid item name or price");
        }
        Item item = items.get(itemName);
        if (item == null) {
            items.put(itemName, new Item(itemName, itemPrice, 1));
        } else {
            item.incrementQuantity();
        }
    }

    public void removeItem(String itemName) {
        if (itemName == null) {
            throw new IllegalArgumentException("Item name cannot be null");
        }
        Item item = items.get(itemName);
        if (item == null) {
            throw new IllegalArgumentException("Item not in cart");
        }
        if (item.getQuantity() == 1) {
            items.remove(itemName);
        } else {
            item.decrementQuantity();
        }
    }

    public double calculateTotalPrice() {
        return items.values().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public void applyDiscount(double discountRate) {
        items.values().forEach(item -> item.setPrice(item.getPrice() * (1 - discountRate)));
    }

    public Map<String, Item> getItems() {
        return items;
    }

    public int getItemQuantity(String itemName) {
        return items.getOrDefault(itemName, new Item(itemName, 0, 0)).getQuantity();
    }

    public void updateQuantity(String itemName, int quantity) {
        if (itemName == null) {
            throw new IllegalArgumentException("Item name cannot be null");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (quantity == 0) {
            items.remove(itemName);
        } else {
            Item item = items.get(itemName);
            if (item != null) {
                item.setQuantity(quantity);
            } else {
                throw new IllegalArgumentException("Item not in cart");
            }
        }
    }

    private static class Item {
        private String name;
        private double price;
        private int quantity;

        public Item(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public void incrementQuantity() {
            this.quantity++;
        }

        public void decrementQuantity() {
            if (this.quantity > 0) {
                this.quantity--;
            }
        }
    }
}

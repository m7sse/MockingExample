package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @Test
    @DisplayName("Should Add Item To Cart")
    void shouldAddItemToCart() {
        cart.addItem("item1", 10.0);
        assertThat(cart.getItems()).containsKey("item1");
    }

    @Test
    @DisplayName("Should Remove Item From Cart")
    void shouldRemoveItemFromCart() {
        cart.addItem("item1", 10.0);
        cart.removeItem("item1");
        assertThat(cart.getItems()).doesNotContainKey("item1");
    }

    @Test
    @DisplayName("Should Calculate Total Price of Items In Cart")
    void shouldCalculateTotalPrice() {
        cart.addItem("item1", 10.0);
        cart.addItem("item2", 15.0);
        double totalPrice = cart.calculateTotalPrice();
        assertThat(totalPrice).isEqualTo(25.0);
    }

    @Test
    @DisplayName("Should Update Item Quantity")
    void shouldUpdateItemQuantity() {
        cart.addItem("item1", 10.0);
        cart.updateQuantity("item1", 5);
        int quantity = cart.getItemQuantity("item1");
        assertThat(quantity).isEqualTo(5);
    }

    @Test
    @DisplayName("Should Throw Exception For Null Items")
    void shouldThrowExceptionForNullItem() {
        assertThatThrownBy(() -> cart.addItem(null, 10.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid item name or price");
    }

    @Test
    @DisplayName("Should Apply Discount Correctly")
    void shouldApplyDiscountCorrectly() {
        cart.addItem("item1", 100.0);
        cart.applyDiscount(0.1);
        double totalPrice = cart.calculateTotalPrice();
        assertThat(totalPrice).isEqualTo(90.0);
    }

    @Test
    @DisplayName("Should Throw Exception When Removing Null Item")
    void shouldThrowExceptionWhenRemovingNullItem() {
        assertThatThrownBy(() -> cart.removeItem(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Item name cannot be null");
    }

    @Test
    @DisplayName("Should Throw Exception When Removing Item Not In Cart")
    void shouldThrowExceptionWhenRemovingItemNotInCart() {
        assertThatThrownBy(() -> cart.removeItem("item1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Item not in cart");
    }

    @Test
    @DisplayName("Should Throw Exception When Updating Null Item")
    void shouldThrowExceptionWhenUpdatingNullItem() {
        assertThatThrownBy(() -> cart.updateQuantity(null, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Item name cannot be null");
    }

    @Test
    @DisplayName("Should Throw Exception When Updating Item With Negative Quantity")
    void shouldThrowExceptionWhenUpdatingItemWithNegativeQuantity() {
        cart.addItem("item1", 10.0);
        assertThatThrownBy(() -> cart.updateQuantity("item1", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity cannot be negative");
    }

    @Test
    @DisplayName("Should Remove Item When Updating Quantity to Zero")
    void shouldRemoveItemWhenUpdatingQuantityToZero() {
        cart.addItem("item1", 10.0);
        cart.updateQuantity("item1", 0);
        assertThat(cart.getItems()).doesNotContainKey("item1");
    }
}

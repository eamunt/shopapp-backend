package com.project.shopapp.repositories;

import com.project.shopapp.models.Order;
import com.project.shopapp.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findOrderById(Long id);
    // Find the orders of any user.
    List<Order> findByUserId(User userId);


    @Query("SELECT o FROM Order o WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "o.fullName LIKE %:keyword% " +
            "OR o.address LIKE %:keyword% " +
            "OR o.note LIKE %:keyword% " +
            "OR o.email LIKE %:keyword%)")
    Page<Order> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

//    @Query("SELECT o FROM Order o WHERE o.active = true AND (:keyword IS NULL OR :keyword = '' OR " +
//            "o.fullName LIKE %:keyword% " +
//            "OR o.address LIKE %:keyword% " +
//            "OR o.note LIKE %:keyword% " +
//            "OR o.email LIKE %:keyword%)")
//    Page<Order> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

/*
INSERT INTO orders (user_id, fullname, email, phone_number, address, note, status, total_money)
VALUES
    (17, 'John Smith', 'john@example.com', '1234567890', '123 Main St', 'Note 1', 'pending', 500),
    (18, 'Eric Thompson', 'eric@example.com', '9876543210', '456 Elm St', 'Note 2', 'pending', 400),
    (17, 'Hans', 'hans@example.com', '5555555555', '789 Oak St', 'Note 3', 'pending', 300),
    (18, 'Alice Johnson', 'alice@example.com', '5551234567', '789 Cherry Ave', 'Note 4', 'pending', 200),
    (17, 'Robert Williams', 'robert@example.com', '5559876543', '321 Maple Rd', 'Note 5', 'pending', 100),
    (17, 'Sarah Davis', 'sarah@example.com', '5554445555', '987 Elm St', 'Note 6', 'pending', 250),
    (18, 'Michael Anderson', 'michael@example.com', '5556667777', '654 Oak Ave', 'Note 7', 'pending', 350),
    (17, 'Emma Wilson', 'emma@example.com', '5558889999', '789 Maple Ln', 'Note 8', 'pending', 450),
    (17, 'Olivia Brown', 'olivia@example.com', '5551112222', '987 Pine St', 'Note 47', 'pending', 350),
    (18, 'William Davis', 'william@example.com', '5553334444', '654 Elm Ave', 'Note 48', 'pending', 250),
    (17, 'Sophia Wilson', 'sophia@example.com', '5555556666', '789 Oak Ln', 'Note 49', 'pending', 150),
    (18, 'Alexander Anderson', 'alexander@example.com', '5557778888', '456 Maple Lane', 'Note 50', 'pending', 450),
    (17, 'Ava Thompson', 'ava@example.com', '5559990000', '987 Walnut Rd', 'Note 51', 'pending', 550),
    (18, 'Daniel Johnson', 'daniel@example.com', '5552223333', '654 Pine Ave', 'Note 52', 'pending', 650),
    (17, 'Mia Williams', 'mia@example.com', '5554445555', '789 Elm St', 'Note 53', 'pending', 750),
    (18, 'James Davis', 'james@example.com', '5556667777', '456 Oak Ave', 'Note 54', 'pending', 850),
    (18, 'Benjamin Thompson', 'benjamin@example.com', '5550001111', '654 Walnut Rd', 'Note 56', 'pending', 550),
    (17, 'Sophia Anderson', 'sophia@example.com', '5551112222', '987 Pine St', 'Note 57', 'pending', 350),
    (18, 'Elijah Davis', 'elijah@example.com', '5553334444', '654 Elm Ave', 'Note 58', 'pending', 250),
    (17, 'Ava Wilson', 'ava@example.com', '5555556666', '789 Oak Ln', 'Note 59', 'pending', 150),
    (18, 'Oliver Thompson', 'oliver@example.com', '5557778888', '456 Maple Lane', 'Note 60', 'pending', 450),
    (17, 'Mia Johnson', 'mia@example.com', '5559990000', '987 Walnut Rd', 'Note 61', 'pending', 550),
    (18, 'James Williams', 'james@example.com', '5552223333', '654 Pine Ave', 'Note 62', 'pending', 650),
    (17, 'Charlotte Davis', 'charlotte@example.com', '5554445555', '789 Elm St', 'Note 63', 'pending', 750),
    (18, 'Benjamin Wilson', 'benjamin@example.com', '5556667777', '456 Oak Ave', 'Note 64', 'pending', 850),
    (17, 'Amelia Thompson', 'amelia@example.com', '5558889999', '321 Maple Ln', 'Note 65', 'pending', 950),
    (18, 'Henry Johnson', 'henry@example.com', '5550001111', '654 Walnut Rd', 'Note 66', 'pending', 550),
    (18, 'Emily Davis', 'emily@example.com', '5552223333', '456 Walnut Lane', 'Note 46', 'pending', 150);

*/

/*
INSERT INTO order_details (order_id, product_id, price, number_of_products, total_money, color)
    VALUES
            (41, 1, 10.99, 2, 21.98, 'Red'),
            (41, 2, 5.99, 3, 17.97, 'Blue'),
            (41, 3, 8.49, 1, 8.49, 'Green'),
            (42, 1, 10.99, 2, 21.98, 'Red'),
            (42, 2, 5.99, 3, 17.97, 'Blue'),
            (42, 3, 8.49, 1, 8.49, 'Green'),
            (43, 6, 12.99, 3, 38.97, 'Purple'),
            (44, 7, 6.99, 1, 6.99, 'Pink'),
            (45, 8, 14.99, 2, 29.98, 'Gray'),
            (46, 9, 11.49, 1, 11.49, 'Brown'),
            (47, 10, 8.99, 3, 26.97, 'Black'),
            (48, 11, 13.99, 2, 27.98, 'Silver'),
            (49, 12, 10.49, 1, 10.49, 'Gold'),
            (50, 13, 7.49, 2, 14.98, 'White'),
            (65, 1, 10.99, 2, 21.98, 'Red'),
            (65, 2, 5.99, 3, 17.97, 'Blue'),
            (65, 3, 8.49, 1, 8.49, 'Green'),
            (51, 14, 9.99, 2, 19.98, 'Red'),
            (51, 15, 5.99, 3, 17.97, 'Blue'),
            (51, 16, 8.49, 1, 8.49, 'Green'),
            (52, 17, 10.99, 2, 21.98, 'Yellow'),
            (52, 18, 5.99, 3, 17.97, 'Orange'),
            (52, 19, 8.49, 1, 8.49, 'Purple'),
            (53, 20, 6.99, 2, 13.98, 'Pink'),
            (53, 21, 14.99, 1, 14.99, 'Gray'),
            (53, 22, 11.49, 3, 34.47, 'Brown'),
            (54, 23, 8.99, 2, 17.98, 'Black'),
            (54, 24, 13.99, 1, 13.99, 'Silver'),
            (54, 25, 10.49, 3, 31.47, 'Gold'),
            (55, 26, 7.49, 2, 14.98, 'White'),
            (55, 27, 9.99, 1, 9.99, 'Red'),
            (55, 28, 5.99, 3, 17.97, 'Blue'),
            (56, 29, 8.49, 1, 8.49, 'Green'),
            (56, 30, 10.99, 2, 21.98, 'Yellow'),
            (56, 31, 5.99, 3, 17.97, 'Orange'),
            (57, 32, 8.49, 1, 8.49, 'Purple'),
            (57, 33, 6.99, 2, 13.98, 'Pink'),
            (57, 34, 14.99, 1, 14.99, 'Gray'),
            (58, 35, 11.49, 3, 34.47, 'Brown'),
            (58, 36, 8.99, 2, 17.98, 'Black'),
            (58, 37, 13.99, 1, 13.99, 'Silver'),
            (59, 38, 10.49, 3, 31.47, 'Gold'),
            (59, 39, 7.49, 2, 14.98, 'White'),
            (59, 40, 9.99, 1, 9.99, 'Red'),
            (60, 41, 5.99, 3, 17.97, 'Blue'),
            (60, 42, 8.49, 1, 8.49, 'Green'),
            (60, 43, 10.99, 2, 21.98, 'Yellow'),
            (61, 44, 5.99, 3, 17.97, 'Orange'),
            (61, 45, 8.49, 1, 8.49, 'Purple'),
            (61, 46, 6.99, 2, 13.98, 'Pink'),
            (62, 47, 14.99, 1, 14.99, 'Gray'),
            (62, 48, 11.49, 3, 34.47, 'Brown'),
            (62, 49, 8.99, 2, 17.98, 'Black'),
            (63, 50, 13.99, 1, 13.99, 'Silver'),
            (63, 51, 10.49, 3, 31.47, 'Gold'),
            (63, 52, 7.49, 2, 14.98, 'White'),
            (64, 53, 9.99, 1, 9.99, 'Red'),
            (64, 54, 5.99, 3, 17.97, 'Blue');
*/
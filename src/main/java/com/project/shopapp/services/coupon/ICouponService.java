package com.project.shopapp.services.coupon;

import com.project.shopapp.models.Coupon;

public interface ICouponService {
    double calculateCouponValue(String couponCode, double totalAmount);
}

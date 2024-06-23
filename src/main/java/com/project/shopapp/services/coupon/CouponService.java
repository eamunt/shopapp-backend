package com.project.shopapp.services.coupon;

import com.project.shopapp.models.Coupon;
import com.project.shopapp.models.CouponCondition;
import com.project.shopapp.repositories.CouponConditionRepository;
import com.project.shopapp.repositories.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService implements ICouponService{
    private final CouponRepository couponRepository;
    private final CouponConditionRepository couponConditionRepository;
    @Override
    public double calculateCouponValue(String couponCode, double totalAmount) {
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        if(!coupon.isActive()) {
            throw new IllegalArgumentException("Coupon is not active");
        }
        double discount = calculateDiscount(coupon, totalAmount);
        double finalAmount = totalAmount - discount;
        return finalAmount;
    }

    private double calculateDiscount(Coupon coupon, double totalAmount) {
        List<CouponCondition> conditions = couponConditionRepository.findByCouponId(coupon.getId());
        double discount = 0.0;
        double updatedTotalAmount = totalAmount;
        for(CouponCondition condition : conditions) {
            // EAV (entity - attribute - value) Model
            String attribute = condition.getAttribute();
            String operator = condition.getOperator();
            String value = condition.getValue();

            double percentDiscount = Double.valueOf(
                    String.valueOf(condition.getDiscountAmount())
            );

//            INSERT INTO coupon_conditions (id, coupon_id, attribute, operator, value, discount_amount)
//            VALUES (1, 1, 'minimum_amount', '>', '100', 10);
//
//            INSERT INTO coupon_conditions (id, coupon_id, attribute, operator, value, discount_amount)
//            VALUES (2, 1, 'applicable_date', 'BETWEEN', '2024-12-25', 5);
//
//            INSERT INTO coupon_conditions (id, coupon_id, attribute, operator, value, discount_amount)
//            VALUES (3, 2, 'minimum_amount', '>', '200', 20);

            if(attribute.equals("minimum_amount")) {
                if(operator.equals(">") && updatedTotalAmount > Double.parseDouble(value)) {
                    discount += updatedTotalAmount * percentDiscount/100;
                }
            }else if(attribute.equals("applicable_date")) {
                LocalDate applicableDate = LocalDate.parse(value);
                LocalDate currentDate = LocalDate.now();

                if(operator.equalsIgnoreCase("BETWEEN") && currentDate.isEqual(applicableDate)) {
                    discount += updatedTotalAmount * percentDiscount/100;
                }
            }
            // we can customize a lot of conditions here
            updatedTotalAmount = updatedTotalAmount - discount;
        }
        return discount;
    }
}

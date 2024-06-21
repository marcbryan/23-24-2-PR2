package uoc.ds.pr.util;

import uoc.ds.pr.ShippingLinePR2;

public class LoyaltyLevel {
    static ShippingLinePR2.LoyaltyLevel getLevel(int level) {
        if (level <= 5)
            return ShippingLinePR2.LoyaltyLevel.BRONZE;
        else if (level <= 10)
            return ShippingLinePR2.LoyaltyLevel.SILVER;
        else if (level <= 15)
            return ShippingLinePR2.LoyaltyLevel.GOLD;
        else
            return ShippingLinePR2.LoyaltyLevel.DIAMOND;
    }
}

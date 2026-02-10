import java.util.ArrayList;
/**
 * Represents a customer account in the platform.
 *
 * Stores customer-specific data such as total spending, loyalty tier,
 * personal blacklist of freelancers, and employment statistics.
 *
 * Loyalty tier is updated based on effective spending, which applies
 * a penalty for customer-initiated cancellations while preserving
 * the original spending total for reporting.
 */

public class Customer {
    public UserHashSet userBlacklist = new UserHashSet();
    public String id ;
    public double spent;
    public String loyaltyTier;
    public ArrayList<String> blacklistIds;
    public int totalEmploymentCount;
    public int cancelAmount;


    public Customer(String id) {
        this.id = id;
        this.spent = 0.0;
        this.loyaltyTier = "BRONZE";
        this.blacklistIds = new ArrayList<>();
        this.totalEmploymentCount = 0;
        this.cancelAmount = 0;

    }

    public void updateLoyaltyLevel() {
        double loyaltyBase = spent - 250.0 * cancelAmount;
        if (loyaltyBase < 0) loyaltyBase = 0;

        if (loyaltyBase >= 5000) {
            loyaltyTier = "PLATINUM";
        } else if (loyaltyBase >= 2000) {
            loyaltyTier = "GOLD";
        } else if (loyaltyBase >= 500) {
            loyaltyTier = "SILVER";
        } else {
            loyaltyTier = "BRONZE";
        }
    }

}

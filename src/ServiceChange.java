
/**
 * Simple data holder representing a queued service change request.
 *
 * Instances of this class are stored until the end of the month
 * and applied atomically during the simulate_month operation.
 */
public class ServiceChange {
    public String newService;
    public int newPrice;

    public ServiceChange(String newService, int newPrice) {
        this.newService = newService;
        this.newPrice = newPrice;
    }
}

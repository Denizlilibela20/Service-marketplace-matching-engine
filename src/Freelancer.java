/**
 * Represents a freelancer profile and its runtime state.
 *
 * Contains the freelancer's service offering, price, skill set,
 * incremental average rating, availability status, and counters
 * used for reliability, burnout, and platform-level blacklisting.
 *
 * The heapIndex field is maintained by FreelancerMaxHeap to allow
 * O(log n) priority updates and removals without searching the heap.
 *
 * Newly registered freelancers start with a single 5-star rating,
 * as defined by the project specification.
 */

public class Freelancer {

    public String id;
    public String serviceType;
    public double rating;
    public int cancelAmount;
    public int skill_Technical;
    public int skill_Communication;
    public int skill_Creativity;
    public int skill_Efficiency;
    public int skill_Detail;
    public int price;
    public int ratingAmount;
    public int completeAmount;
    public boolean isBlackListed;
    public boolean isAvailable;
    public String currentCustomerId;
    public int compositeScore;
    public boolean isBurnout;
    public int monthlyCompleted;
    public int monthlyCancelled;
    public int heapIndex;


    public Freelancer(String id, String service, int price, int T, int C, int R, int E, int A){
        this.serviceType = service;
        this.id = id;
        this.price = price;
        this.skill_Communication =C;
        this.skill_Creativity =R;
        this.skill_Efficiency =E;
        this.skill_Detail =A;
        this.skill_Technical =T;
        this.ratingAmount =1;
        this.rating =5.0;
        this.isBlackListed = false;
        this.isAvailable=true;
        this.currentCustomerId=null;
        this.compositeScore = 0;
    }

    public void addRating(int rating) {

        if (rating < 0) rating = 0;
        if (rating > 5) rating = 5;

        double newAverage = (this.rating * this.ratingAmount + rating) / (this.ratingAmount + 1);

        this.rating = newAverage;
        this.ratingAmount++;
    }

    public void updateAvailableStatus() {isAvailable= !isAvailable;}

}


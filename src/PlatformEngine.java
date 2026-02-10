import java.util.ArrayList;

/**
 * Core engine of the GigMatch Pro platform.
 *
 * This class owns and manages the entire in-memory state of the system, including
 * customers, freelancers, service profiles, ranking heaps, and pending service changes.
 *
 * Each public method corresponds directly to a command defined in the project
 * specification (registration, job requests, employments, cancellations, ratings,
 * blacklisting, skill updates, and month-end simulation).
 *
 * Performance considerations:
 * - Uses custom hash maps for expected O(1) access by user ID.
 * - Maintains a max-heap per service category to retrieve top-ranked freelancers
 *   and update their priorities in O(log n).
 *
 * Composite scores are computed using normalized skill, rating, and reliability
 * components with an optional burnout penalty, and stored as integers to ensure
 * deterministic ordering.
 */


public class PlatformEngine {
    public static UserHashMap<Freelancer> freeHM = new UserHashMap<>(512);
    public static UserHashMap<Customer> customerHM = new UserHashMap<>(512);
    private static UserHashMap<ServiceRequirement> serviceReq = new UserHashMap<>(32);
    private static UserHashMap<FreelancerMaxHeap> globalHeap = new UserHashMap<>(32);
    public static UserHashMap<ServiceChange> pendingServiceChanges = new UserHashMap<>(512);
    static {
        serviceReq.put("paint", new ServiceRequirement(70 ,60 ,50 ,85, 90));
        serviceReq.put("web_dev", new ServiceRequirement(95, 75 ,85 ,80, 90));
        serviceReq.put("graphic_design", new ServiceRequirement(75 ,85, 95 ,70, 85));
        serviceReq.put("data_entry", new ServiceRequirement(50, 50, 30, 95 ,95));
        serviceReq.put("tutoring", new ServiceRequirement(80, 95, 70 ,90, 75));
        serviceReq.put("cleaning", new ServiceRequirement(40 ,60 ,40, 90, 85));
        serviceReq.put("writing", new ServiceRequirement(70, 85, 90, 80, 95));
        serviceReq.put("photography", new ServiceRequirement(85, 80, 90 ,75, 90));
        serviceReq.put("plumbing", new ServiceRequirement(85 ,65, 60, 90, 85));
        serviceReq.put("electrical", new ServiceRequirement(90, 65, 70 ,95 ,95));
    }



    //add freelancer to hashmap
    public String freelancerAdd(String id, String service, int price, int T, int C, int R, int E, int A){
        Freelancer f = new Freelancer(id, service, price, T, C, R, E, A);
        if (!freeHM.contains(id)){
            freeHM.put(id,f);
            FreelancerMaxHeap heap = getHeapForService(service);
            heap.add(f);
            recomputeCompositeScore(f);
            return "registered freelancer " + id ;
        }
        return "Some error occurred in register_freelancer.";

    }
    //add customer to hashmap
    public String customerAdd(String id ){
        Customer customer = new Customer(id);
        if (!customerHM.contains(id)){
            customerHM.put(id,customer);
            return "registered customer " + id ;
        }
        return "Some error occurred in register_customer.";
    }

    public String userBlacklistFreelancer(String customerId, String freelancerId) {
        Customer c = customerHM.get(customerId);
        Freelancer f = freeHM.get(freelancerId);
        if (c == null || f==null) return "Some error occurred in blacklist.";
        if (c.userBlacklist.contains(freelancerId)) {
            return "Some error occurred in blacklist.";
        }
        c.userBlacklist.add(freelancerId);
        return customerId + " blacklisted " + freelancerId;
    }

    public String userUnblacklistFreelancer(String customerId, String freelancerId) {
        Customer c = customerHM.get(customerId);
        Freelancer f = freeHM.get(freelancerId);
        if (c == null || f==null ) return "Some error occurred in unblacklist.";
        if (!c.userBlacklist.contains(freelancerId)) {
            return "Some error occurred in unblacklist.";
        }
        c.userBlacklist.remove(freelancerId);
        return customerId + " unblacklisted " + freelancerId;
    }


    public String employFreelancer(String customerId, String freelancerId) {
        Customer customer = customerHM.get(customerId);
        if (customer == null) {
            return "Some error occurred in employ.";
        }
        Freelancer freelancer = freeHM.get(freelancerId);
        if (freelancer == null) {
            return "Some error occurred in employ.";
        }
        if (customer.userBlacklist.contains(freelancerId)) {
            return "Some error occurred in employ.";
        }
        if (!freelancer.isAvailable) {
            return "Some error occurred in employ.";
        }
        if (freelancer.isBlackListed) {
            return "Some error occurred in employ.";
        }


        freelancer.isAvailable = false;
        freelancer.currentCustomerId = customerId;
        customer.totalEmploymentCount++;
        return customerId + " employed " + freelancerId + " for " + freelancer.serviceType;
    }


    public String queryFreelancer(String id){
        Freelancer f = freeHM.get(id);
        if(f == null )return "Some error occurred in query_freelancer.";
        String status;
        String burnout;
        if (f.isAvailable) status = "yes";
        else
            status ="no";
        if (f.isBurnout) burnout ="yes";
        else
            burnout ="no";
        return f.id+": "+ f.serviceType + ", price: " + f.price + ", rating: " + Math.round(f.rating * 10.0) / 10.0 +", completed: " + f.completeAmount + ", cancelled: "+ f.cancelAmount+", skills: (" +f.skill_Technical+","+f.skill_Communication+","+f.skill_Creativity+","+f.skill_Efficiency+","+ f.skill_Detail+"), available: "+ status + ", burnout: " + burnout;
    }


    public String queryCustomer(String customerId) {
        Customer c = customerHM.get(customerId);
        if (c == null) {
            return "Some error occurred in query_customer.";
        }

        int spent = (int) c.spent;
        int blacklistedCount = c.userBlacklist.size();
        int employmentCount = c.totalEmploymentCount;

        return customerId + ": total spent: $" + spent + ", loyalty tier: " + c.loyaltyTier + ", blacklisted freelancer count: " + blacklistedCount + ", total employment count: " + employmentCount;
    }


    //recalculate the composite score
    public void recomputeCompositeScore(Freelancer f) {
        int oldC = f.compositeScore;
        FreelancerMaxHeap heap = globalHeap.get(f.serviceType);
        ServiceRequirement s = getServiceProfile(f.serviceType);


        double skillScore = computeSkillScore(f, s);
        double ratingScore = computeRatingScore(f);
        double reliabilityScore = computeReliabilityScore(f);
        double burnoutPenalty = computeBurnoutPenalty(f);

        double compositeScore = 10000.0 * (0.55 * skillScore + 0.25 * ratingScore + 0.20 * reliabilityScore - burnoutPenalty);

        if (compositeScore < 0.0) compositeScore = 0.0;
        if (compositeScore > 10000.0) compositeScore = 10000.0;

        f.compositeScore = (int) Math.floor(compositeScore);
        if(heap!=null) heap.updateHeap(f,oldC);
    }

    public String updateSkill(String freelancerId, int T, int C, int R, int E, int A) {
        Freelancer f = freeHM.get(freelancerId);
        if (f == null) {
            return "Some error occurred in update_skill.";
        }

        if (!isValidSkill(T) || !isValidSkill(C) || !isValidSkill(R) || !isValidSkill(E) || !isValidSkill(A)) {
            return "Some error occurred in update_skill.";
        }
        f.skill_Technical = T;
        f.skill_Communication = C;
        f.skill_Creativity = R;
        f.skill_Efficiency = E;
        f.skill_Detail = A;

        recomputeCompositeScore(f);

        return "updated skills of " + freelancerId + " for " + f.serviceType;
    }

    private boolean isValidSkill(int s) {
        return s >= 0 && s <= 100;
    }

    private static ServiceRequirement getServiceProfile(String stype){
        return serviceReq.get(stype);
    }

    private double computeSkillScore(Freelancer f, ServiceRequirement s) {
        int freeT = f.skill_Technical;
        int freeC = f.skill_Communication;
        int freeR = f.skill_Creativity;
        int freeE = f.skill_Efficiency;
        int freeA = f.skill_Detail;

        int reqT = s.t;
        int reqC = s.c;
        int reqR = s.r;
        int reqE = s.e;
        int reqA = s.a;

        int dotProduct = (freeT * reqT) + (freeC * reqC) + (freeR * reqR) + (freeE * reqE) + (freeA *reqA);
        int sumReq = reqT + reqC + reqR + reqE + reqA;

        return dotProduct / (100.0 * sumReq);
    }

    private double computeRatingScore(Freelancer f) {
        return f.rating / 5.0;
    }


    private double computeReliabilityScore(Freelancer f) {
        int completedTotal = f.completeAmount;
        int cancelledTotal = f.cancelAmount;
        int total = completedTotal + cancelledTotal;

        if (total == 0) {
            return 1.0;
        }
        return 1.0 - (double) cancelledTotal / total;
    }

    private double computeBurnoutPenalty(Freelancer f) {
        if(f.isBurnout){ return  0.45;}
        else {
            return  0.0;
        }
    }


    public String freelancerCancel(String id ){
        Freelancer f = freeHM.get(id);
        if (f ==null ||f.isAvailable ) return "Some error occurred in cancel_by_freelancer.";
        f.addRating(0);
        f.cancelAmount++;
        f.monthlyCancelled++;
        String cancelledCustomer = f.currentCustomerId;
        f.currentCustomerId = null;

        f.updateAvailableStatus();

        //used math max to prevent getting negative nums
        f.skill_Creativity =Math.max(0,f.skill_Creativity-3);
        f.skill_Communication = Math.max(0,f.skill_Communication-3);
        f.skill_Detail = Math.max(0,f.skill_Detail-3);
        f.skill_Efficiency = Math.max(0,f.skill_Efficiency-3);
        f.skill_Technical = Math.max(0,f.skill_Technical-3);
        String result = "cancelled by freelancer: " + f.id + " cancelled " + cancelledCustomer;

        //blacklist status
        if(f.monthlyCancelled>=5) {
            f.isBlackListed = true;
            FreelancerMaxHeap heap = globalHeap.get(f.serviceType);
            if (heap != null) {
                heap.remove(f);
            }
            result += "\nplatform banned freelancer: " + f.id;
        }
        recomputeCompositeScore(f);

        return result;
    }

    public String customerCancel(String c_id, String f_id){
        Freelancer f = freeHM.get(f_id);
        Customer c = customerHM.get(c_id);
        if(c==null || f==null || f.isAvailable ||!c_id.equals(f.currentCustomerId)) return "Some error occurred in cancel_by_customer.";
        f.isAvailable = true;
        c.cancelAmount++;
        f.currentCustomerId = null;
        return "cancelled by customer: " + c_id + " cancelled " + f_id;

    }

    //i put service change request and apply them at the end of the month
    public String changeService(String freelancerId, String newService, int newPrice) {
        Freelancer f = freeHM.get(freelancerId);

        if (f == null) {
            return "Some error occurred in change_service.";
        }

        ServiceRequirement req = serviceReq.get(newService);
        if (req == null) {
            return "Some error occurred in change_service.";
        }

        if (newPrice <= 0) {
            return "Some error occurred in change_service.";
        }

        String oldService = f.serviceType;

        pendingServiceChanges.put(freelancerId, new ServiceChange(newService, newPrice));


        return "service change for " + freelancerId +
                " queued from " + oldService + " to " + newService;
    }


    public String compeleteAndRate(String f_id, int rating){
        Freelancer f  = freeHM.get(f_id);
        if(f==null) return "Some error occurred in complete_and_rate.";
        if (f.isAvailable || f.currentCustomerId == null) {
            return "Some error occurred in complete_and_rate.";
        }
        String customerId = f.currentCustomerId;
        Customer c = customerHM.get(customerId);
        f.completeAmount++;
        f.monthlyCompleted++;
        f.isAvailable = true;
        f.addRating(rating);

        if (rating>=4){
            applySkillEvos(f);
        }

        double subsidyRate = getSubsidyRate(c.loyaltyTier);
        int price = f.price;
        int customerPayment = (int) Math.floor(price * (1.0 - subsidyRate));
        c.spent += customerPayment;

        f.currentCustomerId = null;
        f.isAvailable = true;
        recomputeCompositeScore(f);
        return f_id + " completed job for " + customerId + " with rating " + rating;

    }
    private double getSubsidyRate(String loyaltyTier) {
        if ("PLATINUM".equals(loyaltyTier)) {
            return 0.15;
        } else if ("GOLD".equals(loyaltyTier)) {
            return 0.10;
        } else if ("SILVER".equals(loyaltyTier)) {
            return 0.05;
        } else {
            return 0.0;
        }
    }
    //skill evoluations after job done with 4> rating
    private void applySkillEvos(Freelancer f) {
        ServiceRequirement service = serviceReq.get(f.serviceType);

        //value,index pairs
        int[][] pairs = {{ service.t, 0}, { service.c, 1},{ service.r,2},{ service.e, 3},{ service.a,4}};

        // finding max 2. and 3. elemnents
        for (int i = 0; i < 5; i++) {
            int best = i;
            for (int j = i + 1; j < 5; j++) {
                int valJ = pairs[j][0], idxJ = pairs[j][1];
                int valB = pairs[best][0], idxB = pairs[best][1];

                // if values same use tiebreak(index)
                if (valJ > valB || (valJ == valB && idxJ < idxB)) {
                    best = j;
                }
            }
            int[] temp = pairs[i];
            pairs[i] = pairs[best];
            pairs[best] = temp;
        }

        int primaryIndex = pairs[0][1];
        int secondIndex = pairs[1][1];
        int thirdIndex = pairs[2][1];

        //freelancer skills
        int[] skills = {f.skill_Technical, f.skill_Communication, f.skill_Creativity, f.skill_Efficiency, f.skill_Detail};


        for (int i = 0; i < 5; i++) {
            if (i == primaryIndex) {
                skills[i] += 2;
            } else if (i == secondIndex || i == thirdIndex) {
                skills[i] += 1;
            }
            if (skills[i] > 100) skills[i] = 100;
        }

        f.skill_Technical= skills[0];
        f.skill_Communication = skills[1];
        f.skill_Creativity= skills[2];
        f.skill_Efficiency = skills[3];
        f.skill_Detail = skills[4];

        recomputeCompositeScore(f);
    }
    private FreelancerMaxHeap getHeapForService(String serviceType) {
        FreelancerMaxHeap heap = globalHeap.get(serviceType);
        if (heap == null) {
            heap = new FreelancerMaxHeap();
            globalHeap.put(serviceType, heap);
        }
        return heap;
    }


    //request job method
    public String requestJob(String customerId, String serviceType, int topK) {
        Customer c = customerHM.get(customerId);
        FreelancerMaxHeap heap = globalHeap.get(serviceType);
        ServiceRequirement s = serviceReq.get(serviceType);

        if (c == null) {
            return "Some error occurred in request_job.";
        }

        if (s == null) {
            return "Some error occurred in request_job.";
        }

        if (heap == null || heap.isEmpty()) {
            return "no freelancers available";
        }

        ArrayList<Freelancer> popped = new ArrayList<>();
        ArrayList<Freelancer> selected = new ArrayList<>();

        while (!heap.isEmpty() && selected.size() < topK) {
            Freelancer f = heap.pull();
            if (f == null) break;
            popped.add(f);

            // unwanted conditions
            if (!f.serviceType.equals(serviceType)) continue;
            if (f.isBlackListed) continue;
            if (f.isBurnout) continue;
            if (!f.isAvailable) continue;
            if (c.userBlacklist.contains(f.id)) continue;

            selected.add(f);
        }

        for (Freelancer f: popped) {
            heap.add(f);
        }

        if (selected.isEmpty()) {
            return "no freelancers available";
        }

        //this is for checking if that mych amount is freelancer available, if no, use the selected.size
        int take = Math.min(topK, selected.size());

        StringBuilder sb = new StringBuilder();
        sb.append("available freelancers for ").append(serviceType).append(" (top ").append(take).append(")").append(":\n");

        for (int i = 0; i < take; i++) {
            Freelancer f = selected.get(i);
            String ratingStr = String.format(java.util.Locale.US, "%.1f", f.rating);
            sb.append(f.id).append(" - ").append("composite: ").append(f.compositeScore).append(", price: ").append(f.price).append(", rating: ").append(ratingStr).append("\n");
        }

        Freelancer best = selected.getFirst();
        best.isAvailable = false;
        best.currentCustomerId = customerId;
        c.totalEmploymentCount++;

        sb.append("auto-employed best freelancer: ").append(best.id).append(" for customer ").append(customerId);

        return sb.toString();
    }



    public String simulateMonth() {
        ArrayList<Freelancer> allFreelancers = freeHM.values();
        for (Freelancer f :allFreelancers) {
            if (!f.isBurnout && f.monthlyCompleted >= 5) {
                f.isBurnout = true;
            }

            else if (f.isBurnout && f.monthlyCompleted <= 2) {
                f.isBurnout = false;
            }

            f.monthlyCompleted = 0;
            f.monthlyCancelled = 0;
            recomputeCompositeScore(f);
        }

        //loyality updates
        ArrayList<Customer> allCustomers = customerHM.values();
        for (Customer c: allCustomers) {
            c.updateLoyaltyLevel();
        }

        //service changes
        ArrayList<String> changeKeys = pendingServiceChanges.keys();
        for (String fid : changeKeys) {
            ServiceChange sc = pendingServiceChanges.get(fid);
            if (sc == null) continue;

            Freelancer f = freeHM.get(fid);
            String oldService = f.serviceType;

            FreelancerMaxHeap oldHeap = globalHeap.get(oldService);
            if (oldHeap != null) {
                oldHeap.remove(f);
            }

            f.serviceType = sc.newService;
            f.price = sc.newPrice;

            FreelancerMaxHeap newHeap = getHeapForService(sc.newService);
            newHeap.add(f);

            recomputeCompositeScore(f);
        }

        pendingServiceChanges = new UserHashMap<>(512);

        return "month complete";
    }

}

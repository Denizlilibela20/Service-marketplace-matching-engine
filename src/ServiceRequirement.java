/**
 * Represents the predefined skill requirement profile of a service category.
 *
 * Each service is defined by five integer requirement values
 * (Technical, Communication, Creativity, Efficiency, Attention to Detail),
 * which are used for skill matching and post-job skill evolution.
 *
 */
public class ServiceRequirement {
    int t;
    int r;
    int c;
    int e;
    int a;
    public ServiceRequirement(int t, int c, int r, int e, int a){
        this.t = t;
        this.r=r;
        this.c=c;
        this.e=e;
        this.a=a;
    }

}

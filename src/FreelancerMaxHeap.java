import java.util.ArrayList;
/**
 * Max-heap priority queue used to rank freelancers within a service category.
 *
 * Freelancers are ordered by:
 * 1) Higher composite score
 * 2) Lexicographically smaller freelancer ID as a tie-breaker
 *
 * Supports insertion, extraction, priority updates, and arbitrary removals
 * in O(log n) time by tracking each freelancer's heap index.
 */

public class FreelancerMaxHeap {
    public ArrayList<Freelancer> heap = new ArrayList<>();

    private int parent(int i) {
        return (i-1) /2;
    }
    private int leftChild(int i) {
        return (2*i)+1;
    }
    private int rightChild(int i) {
        return (2 * i)+2;
    }
    public void swap(int i,int j) {
        if (i ==j) return;

        Freelancer fi = heap.get(i);
        Freelancer fj = heap.get(j);

        heap.set(i,fj);
        heap.set(j,fi);

        fi.heapIndex = j;
        fj.heapIndex = i;
    }

    private boolean compareFreelancer(Freelancer a, Freelancer b) {
        if (a.compositeScore != b.compositeScore) {
            return a.compositeScore > b.compositeScore;
        }
        //tiebreak
        return a.id.compareTo(b.id) < 0;
    }
    private void percUp(int i){
        while(i>0) {
            if (compareFreelancer(heap.get(i), heap.get(parent(i)))) {
                swap(i, parent(i));
                i = parent(i);
            }
            else{
                break;
            }
        }
    }

    // i check if left child exists for while loop, if so comp it with right child and decide the best one.
    private void heapify(int i){
        int size = heap.size();
        while (leftChild(i)<size){
            int goldenChild = leftChild(i);
            if (rightChild(i)<size && compareFreelancer(heap.get(rightChild(i)),heap.get(leftChild(i))) ){
                goldenChild = rightChild(i);}
            if(compareFreelancer(heap.get(i),heap.get(goldenChild))){break;}
            swap(i, goldenChild);
            i = goldenChild;
        }
    }

    public void add(Freelancer f){
        heap.add(f);
        f.heapIndex = heap.size()-1;
        percUp(heap.size()-1);
    }

    public Freelancer pull(){
        if (heap.isEmpty()) return null;
        Freelancer root = heap.getFirst();
        Freelancer last = heap.removeLast();
        root.heapIndex = -1; // -1 means its not in the heap anymore

        if (!heap.isEmpty()) {
            heap.set(0, last);
            last.heapIndex = 0;
            heapify(0);
        }
        return root;
    }
    public boolean isEmpty(){
        return heap.isEmpty();
    }

    //deciding to do percup or heapify according to change in comp score
    public void updateHeap(Freelancer f, int oldC) {
        int index = f.heapIndex;

        if (index < 0 || index >= heap.size()) return;

        if (f.compositeScore > oldC) {
            percUp(index);
        } else if (f.compositeScore < oldC) {
            heapify(index);
        }
    }


    public void remove(Freelancer f) {
        int index = f.heapIndex;
        if (index < 0 || index >= heap.size()) {
            return;
        }

        int lastIndex = heap.size() - 1;
        Freelancer last = heap.remove(lastIndex);
        f.heapIndex = -1;
        if (index < heap.size()) {
            heap.set(index, last);
            last.heapIndex = index;
            //cannot decide to do percup or heapify, so i apply both,( if its not suitable method will stop and dont cause an error so and soforth.)
            percUp(index);
            heapify(index);
        }
    }

}

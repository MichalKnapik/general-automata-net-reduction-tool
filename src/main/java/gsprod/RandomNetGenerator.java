package gsprod;

import java.util.ArrayList;

public class RandomNetGenerator {

    private int minbf;
    private int maxbf;
    private int depth;
    private int minautsize;
    private int maxautsize;

    public RandomNetGenerator(int minbf, int maxbf, int depth, int minautsize, int maxautsize) {
        this.minbf = minbf;
        this.maxbf = maxbf;
        this.depth = depth;
        this.minautsize = minautsize;
        this.maxautsize = maxautsize;
    }

  /*  public AutomataNet generate() {
        AutomataNet net = new AutomataNet();

        // at this stage the synchronisation structure is limited: each child synchronizes over a single action with
        // its root

//
//
//        ArrayList<Automaton>

        new RandomAutomatonGenerator(this.minautsize, this.maxautsize);


        // dla kazdego

    }

*/
}

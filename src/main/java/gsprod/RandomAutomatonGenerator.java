package gsprod;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomAutomatonGenerator {

    /**
     * The probability that a new state will be discovered during a step of random graph generation. If a new state is
     * not generated, then a link to some old one is produced.
     */
    private static double exploreNewProbability = 0.5;

    /**
     * The probability that a newly created transition will be a synchronizing one.
     */
    private static double chooseSynchTransProbability = 0.5;

    /**
     * A technical detail: after the initial graph-building the program will run for this many steps adding transitions
     * between existing nodes.
     */
    private static int closingTrans = 10;

    private static String tau = "tau";

    public static void setExplorationAndSynchronizationProbabilities(double explP, double synchP, int closingTransS) {
        exploreNewProbability = explP;
        chooseSynchTransProbability = synchP;
        closingTrans = closingTransS;
    }

    private int minAutoSize;
    private int maxAutosize;

    public RandomAutomatonGenerator(int minAutoSize, int maxAutosize) {
        this.minAutoSize = minAutoSize;
        this.maxAutosize = maxAutosize;
    }

    public Automaton generate(LinkedHashSet<String> syncActions) {
        Random rng = new Random();

        String[] syncActsArray = new String[syncActions.size()];
        syncActsArray = syncActions.toArray(syncActsArray);
        int noOfStates = rng.nextInt(this.maxAutosize - this.minAutoSize + 1) + this.minAutoSize;
        HashSet<Transition> transitions = new HashSet<>();

        int ctr = 1;
        while (ctr < noOfStates) {

            // choose a random state from already existing and reachable
            String currState = Integer.toString(rng.nextInt(ctr));
            String nextState;
            String transLabel = tau;

            // generate a new state or connect to some earlier state
            if (rng.nextDouble() < exploreNewProbability) nextState = Integer.toString(ctr++);
            else nextState = Integer.toString(rng.nextInt(ctr));

            // choose a proper label for the generated transition
            if (rng.nextDouble() < chooseSynchTransProbability && syncActsArray.length != 0)
                transLabel = syncActsArray[rng.nextInt(syncActsArray.length)];

            transitions.add(new Transition(currState, transLabel, nextState));

        }

        // now, add some more random transitions
        for (int i=0; i < closingTrans; ++i) {
            String currState = Integer.toString(rng.nextInt(noOfStates));
            String nextState = Integer.toString(rng.nextInt(noOfStates));
            String transLabel = tau;

            if (rng.nextDouble() < chooseSynchTransProbability && syncActsArray.length != 0)
                transLabel = syncActsArray[rng.nextInt(syncActsArray.length)];

            transitions.add(new Transition(currState, transLabel, nextState));
        }

        ArrayList<String> states = IntStream.range(0, noOfStates).boxed().map(String::valueOf).collect(
                Collectors.toCollection(ArrayList::new));
        System.out.println(states);
        ArrayList<Transition> transitionsF = transitions.stream().collect(Collectors.toCollection(ArrayList::new));

        return new Automaton(states, transitionsF);
    }

    public static void main(String[] args) {
        Scanner myInput = new Scanner(System.in);
        int minStates, maxStates;
        System.out.println("Min st. size: ");
        minStates = myInput.nextInt();
        System.out.println("Max st. size: ");
        maxStates = myInput.nextInt();

        LinkedHashSet<String> syncActions = new LinkedHashSet<String>();
        syncActions.add("one");
        syncActions.add("two");
        syncActions.add("thwee");
        RandomAutomatonGenerator rag = new RandomAutomatonGenerator(minStates, maxStates);
        System.out.println(rag.generate(syncActions));
    }

}

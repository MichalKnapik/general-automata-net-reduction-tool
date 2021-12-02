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

    private static String tau = "tau";

    public static void setExplorationAndSynchronizationProbabilities(double explP, double synchP) {
        exploreNewProbability = explP;
        chooseSynchTransProbability = synchP;
    }

    private int minAutoSize;
    private int maxAutosize;
    private boolean liveReset;

    public RandomAutomatonGenerator(int minAutoSize, int maxAutosize, boolean liveReset) {
        this.minAutoSize = minAutoSize;
        this.maxAutosize = maxAutosize;
        this.liveReset = liveReset;
    }

    public Automaton generate(LinkedHashSet<String> syncActions) {
        // this might need a rewrite
        // note: the first action of syncActions is treated as the (only) reset action is liveReset is set
        Random rng = new Random();

        HashMap<String, Boolean> usedAct = new HashMap<>();
        for (String action: syncActions) usedAct.put(action, false);

        String[] syncActsArray = new String[syncActions.size()];
        syncActsArray = syncActions.toArray(syncActsArray);
        int noOfStates = rng.nextInt(this.maxAutosize - this.minAutoSize + 1) + this.minAutoSize;
        HashSet<Transition> transitions = new HashSet<>();

        int ctr = 1;
        String init = "1";
        while (ctr <= noOfStates) {

            // choose a random state from already existing and reachable
            String currState = Integer.toString(rng.nextInt(ctr) + 1);
            String nextState;
            String transLabel = tau;

            // choose a proper label for the generated transition
            if (rng.nextDouble() < chooseSynchTransProbability && syncActsArray.length != 0) {
                String newLabel = syncActsArray[rng.nextInt(syncActsArray.length)];
                usedAct.put(newLabel, true);
                transLabel = newLabel;
            }

            // generate a new state or connect to some earlier state
            if (this.liveReset && syncActsArray.length > 0 && transLabel.equals(syncActsArray[0])) { // resetting action
                nextState = init;
            } else { // other actions
                if (rng.nextDouble() < exploreNewProbability) nextState = Integer.toString(ctr++);
                else nextState = Integer.toString(rng.nextInt(ctr) + 1);
            }

            transitions.add(new Transition(currState, transLabel, nextState));
        }

        // now, add some more random transitions is there is a transition label that hasn't been used
        while (usedAct.entrySet().stream().anyMatch(e -> !e.getValue())) {
            String currState = Integer.toString(rng.nextInt(noOfStates) + 1);
            String nextState = Integer.toString(rng.nextInt(noOfStates) + 1);
            String transLabel = tau;

            if (rng.nextDouble() < chooseSynchTransProbability) {
                transLabel = syncActsArray[rng.nextInt(syncActsArray.length)];
                usedAct.put(transLabel, true);
            }
            transitions.add(new Transition(currState, transLabel, nextState));
        }

        ArrayList<String> states = IntStream.range(1, noOfStates + 1).boxed().map(String::valueOf).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Transition> transitionsF = transitions.stream().collect(Collectors.toCollection(ArrayList::new));

        return new Automaton(states, transitionsF);
    }

}
package gsprod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class Automaton {

    private String name;
    private ArrayList<String> states;
    private HashSet<String> actionLabels;
    private boolean automataMarking;
    private String initial;
    private HashMap<String, ArrayList<Transition>> stateToTransitions;
    private HashMap<String, Boolean> stateMarkings;
    private static int ctr = 0;

    public Automaton(ArrayList<String> states, ArrayList<Transition> transitions) {
        this.states = (ArrayList<String>) states.stream().distinct().collect(Collectors.toList());
        this.initial = this.states.get(0);
        this.stateMarkings = new HashMap<>();
        this.resetStateMarkings();

        this.actionLabels = new HashSet<>();
        this.stateToTransitions = new HashMap<>();
        for (Transition tran: transitions) this.addTransition(tran);

        this.name = Integer.toString(++ctr);
    }

    public Automaton() {
        this.states = new ArrayList<>();
        this.stateMarkings = new HashMap<String, Boolean>();
        this.actionLabels = new HashSet<>();
        this.stateToTransitions = new HashMap<>();
        this.name = Integer.toString(++ctr);
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getStates() {
        return this.states;
    }

    public void addState(String state) {
        this.getStates().add(state);
        this.stateMarkings.put(state, false);
    }

    public String getInitial() {
        return this.states.get(0);
    }

    public HashSet<String> getActionLabels() {
        return actionLabels;
    }

    public void setInitial(String initial) {
        if (!this.states.contains(initial)) throw new RuntimeException("Setting unknown initial state:" + initial + ".");
        this.initial = initial;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, ArrayList<Transition>> getStateToTransitions() {
        return this.stateToTransitions;
    }

    public void addTransition(Transition tran) {
        String state = tran.getSource();
        String target = tran.getTarget();
        String label = tran.getLabel();

        if (!this.getStateToTransitions().containsKey(state)) this.getStateToTransitions().put(state, new ArrayList<>());
        if (!this.getStateToTransitions().containsKey(target)) this.getStateToTransitions().put(target, new ArrayList<>());

        if (!this.states.contains(state) || !this.states.contains(target))
            throw new RuntimeException("Unknown source or target in transition " + tran + ".");

        this.getStateToTransitions().get(state).add(tran);
        this.getActionLabels().add(label);
    }

    public void addTransition(String source, String label, String target) {
        this.addTransition(new Transition(source, label, target));
    }

    /**
     * Removes from the states and transitions those that are unreachable from the initial state.
     * Fixes bad automata, e.g.: if states = [1,2,3] and transitions = {(1,a,2), (1,b,4)} then the result of running
     * this method will be states = [1,2] and transitions = {(1,a,2)}.
     */
    public void removeUnreachable() {
        this.resetStateMarkings();
        ArrayList<String> frontier = new ArrayList<>();
        frontier.add(this.initial);

        while (!frontier.isEmpty()) {
            String currState = frontier.remove(0);
            if (this.getStateToTransitions().get(currState) != null) {
                for (Transition tran: this.getStateToTransitions().get(currState)) {
                    String target = tran.getTarget();
                    if (!this.isStateMarked(target)) frontier.add(target);
                }
            }
            this.markState(currState);
        }

        this.states = (ArrayList<String>) this.states.stream().filter(state -> this.isStateMarked(state)).collect(Collectors.toList());

        HashMap<String, ArrayList<Transition>> stateToTransitionsPruned = new HashMap<>();
        for (String state: this.getStates()) {
            if (this.isStateMarked(state)) {
                stateToTransitionsPruned.put(state, new ArrayList<>());
                if (this.getStateToTransitions().containsKey(state))
                    for (Transition tran: this.getStateToTransitions().get(state)) {
                        if (this.isStateMarked(tran.getSource()) && this.isStateMarked(tran.getTarget()))
                            stateToTransitionsPruned.get(state).add(tran);
                    }
            }
        }

        this.stateToTransitions = stateToTransitionsPruned;
    }

    @Override
    public String toString() {
        String str = "automaton " + this.name + " with states (the first one is initial):\n"
                + this.getStates().toString() + "\nand transitions:";
        for (String key: this.getStateToTransitions().keySet())
            str += "\n" + key + ": " + this.getStateToTransitions().get(key).toString();

        return str;
    }

    public int countTransitions() {
        return this.getStateToTransitions().values().stream().map(ArrayList::size).reduce(0, Integer::sum);
    }

    /**
     * Changes the (often complex) names of the states to numbers, starting from 0.
     * Also resets state markings.
     */
    public void remapStates() {
        // might need rewrite, if time permits
        ArrayList<String> newStates = new ArrayList<>();
        HashMap<String, ArrayList<Transition>> newStateToTransitions = new HashMap<>();
        HashMap<String, String> helperOldStateToNew = new HashMap<>();


        for (int i=0; i < this.getStates().size(); ++i) {
            String newState = String.valueOf(i);
            newStates.add(newState);
            newStateToTransitions.put(newState, new ArrayList<>());
            helperOldStateToNew.put(this.getStates().get(i), newState);
        }

        for (int i=0; i < newStates.size(); ++i) {
            String currOldState = this.getStates().get(i);
            String currNewState = newStates.get(i);
            ArrayList<Transition> currNewStateTransitions = newStateToTransitions.get(currNewState);

            for (Transition tran: this.getStateToTransitions().get(currOldState)) {
                String newTargetState = helperOldStateToNew.get(tran.getTarget());
                currNewStateTransitions.add(new Transition(currNewState, tran.getLabel(), newTargetState));
            }

            newStateToTransitions.put(currNewState, currNewStateTransitions);
        }

        this.states = newStates;
        this.stateToTransitions = newStateToTransitions;
        this.resetStateMarkings();
    }

    /**
     * Computes and returns those actions of this automaton that either do not belong to syncActions or are known to the other gsprod.Automaton.
     */
    public HashSet<String> getLocalActions(HashSet<String> syncActions, Automaton other) {
        HashSet<String> othersActions = new HashSet<>(other.getActionLabels());
        HashSet<String> localActions = new HashSet<>(this.getActionLabels());
        othersActions.retainAll(syncActions);
        localActions.removeAll(othersActions);

        return localActions;
    }

    /**
     * Computes and returns those actions of this automaton that either do not belong to syncActions or are known to any other gsprod.Automaton.
     */
    public HashSet<String> getLocalActions(HashSet<String> syncActions, ArrayList<Automaton> others) {
        HashSet<String> othersActions = others.stream().map(Automaton::getActionLabels)
                                                       .reduce(new HashSet<String>(), (subtotal, elt) ->
                                                               { subtotal.addAll(elt); return subtotal;});
        HashSet<String> localActions = new HashSet<>(this.getActionLabels());
        othersActions.retainAll(syncActions);
        localActions.removeAll(othersActions);

        return localActions;
    }


    /**
     * Computes and returns those actions of this automaton that belong to syncActions and are known to the other gsprod.Automaton.
     */
    public HashSet<String> getSyncActions(HashSet<String> syncActions, Automaton other) {
        HashSet<String> localActions = new HashSet<>(this.getActionLabels());
        HashSet<String> othersActions = new HashSet<>(other.getActionLabels());
        othersActions.retainAll(syncActions);
        localActions.retainAll(othersActions);

        return localActions;
    }

    // these are labels of automata
    public boolean isMarked() {
        return this.automataMarking;
    }

    public void mark() {
        this.automataMarking = true;
    }

    public void unmark() {
        this.automataMarking = false;
    }

    // these are labels of states of automata
    public void resetStateMarkings() {
        for (String state: this.getStates()) this.stateMarkings.put(state, false);
    }

    /**
     * Returns true iff state is *not present* or is marked.
     */
    public boolean isStateMarked(String state) {
        return this.states.contains(state) && this.stateMarkings.get(state);
    }

    /**
     * Marks an *existing* state.
     */
    public void markState(String state) {
        this.stateMarkings.put(state, true);
    }

}
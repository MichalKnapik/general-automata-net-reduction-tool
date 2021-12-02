package gsprod;

import java.util.ArrayList;

public class GSQState {

    private Automaton root;
    private Automaton activeChild;
    private String rootState;
    private String activeChildState;
    private ArrayList<Automaton> children;
    private ArrayList<String> memoryUnit;

    public GSQState() {
        this.memoryUnit = new ArrayList<>();
    }

    public GSQState(Automaton root, Automaton activeChild, ArrayList<Automaton> children, ArrayList<String> memoryUnit) {
        this();
        this.root = root;
        this.activeChild = activeChild;
        this.children = children;
        this.memoryUnit = memoryUnit;
        this.setRootState(root.getInitial());
        this.setActiveChildState(activeChild.getInitial());
    }

    public GSQState(GSQState state) {
        this.root = state.root;
        this.activeChild = state.activeChild;
        this.children = state.getChildren();
        this.memoryUnit = new ArrayList<>(state.getMemoryUnit());
        this.setActiveChildState(state.getActiveChildState());
        this.setRootState(state.getRootState());
    }

    public Automaton getRoot() {
        return this.root;
    }

    public Automaton getActiveChild() {
        return this.activeChild;
    }

    public void setActiveChild(Automaton child) {
        if (!this.getChildren().contains(child)) throw new RuntimeException("Can't update active child to non-child.");
        this.activeChild = child;
    }

    public String getRootState() {
        return this.rootState;
    }

    public String getActiveChildState() {
        return this.activeChildState;
    }

    public ArrayList<Automaton> getChildren() {
        return this.children;
    }

    public void setChildren(ArrayList<Automaton> children) {
        this.children = children;
    }
    public ArrayList<String> getMemoryUnit() {
        return this.memoryUnit;
    }

    public void setRootState(String rootState) {
        this.rootState = rootState;
    }

    public void setActiveChildState(String activeChildState) {
        this.activeChildState = activeChildState;
    }

    public void updateChildMemory(Automaton child, String newState) {
        // rewrite when time permits
        assert(children.size() == memoryUnit.size());
        if (!child.getStates().contains(newState)) {
            throw new RuntimeException("Can't update memory: no state " + newState + ".");
        }

        for (int i = 0; i < this.getChildren().size(); ++i) {
            if (this.getChildren().get(i) == child) {
                this.getMemoryUnit().set(i, newState);
                return;
            }
        }

        throw new RuntimeException("Can't update memory of unknown child: " + child.getName() + ".");
    }

    public String getChildMemory(Automaton child) {
        // rewrite when time permits
        assert(children.size() == memoryUnit.size());

        for (int i = 0; i < this.getChildren().size(); ++i)
            if (this.getChildren().get(i) == child) return this.getMemoryUnit().get(i);

        throw new RuntimeException("Can't fetch memory of unknown child: " + child.getName() + ".");
    }

    public String toString() {
        String rootName = this.root == null? "": this.root.getName();
        String activeChildName = this.activeChild == null? "": this.activeChild.getName();

        return "[root " + rootName + ":" + this.rootState + ", child " + activeChildName + ":" + this.activeChildState
                + "] mem: " + this.getMemoryUnit().toString();
    }

}
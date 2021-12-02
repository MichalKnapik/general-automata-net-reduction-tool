package gsprod;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class GSQProduct {

    public static Automaton singleLevelProduct(Automaton root, ArrayList<Automaton> children, LinkedHashSet<String> syncActions, boolean verbose) {
        if (verbose) {
            System.out.print("reducing the subtree of " + root.getName() + " with children (statespace size, no of transitions): ");
            for (Automaton child: children)
                System.out.print("(" + child.getStates().size() + ", " + child.countTransitions()+  ") ");
            System.out.println();
        }
        if (children == null) return root;

        // this is the product automaton
        Automaton product = new Automaton();

        // make the dummy initial state
        String init = "init";
        product.addState(init);
        product.setInitial(init);
        product.markState(init);

        // connect the dummy init with pairs of inits of root and each child
        ArrayList<String> initMem = children.stream().map(Automaton::getInitial).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<GSQState> frontier = new ArrayList<>();

        for (Automaton child: children) {
            GSQState childInit = new GSQState(root, child, children, initMem);
            product.addState(childInit.toString());
            frontier.add(childInit);
            product.markState(childInit.toString());
            product.addTransition(init, "epsilon", childInit.toString());
        }

        // build the graph
        while (!frontier.isEmpty()) {
            GSQState currState = frontier.remove(0);
            Automaton child = currState.getActiveChild();

            // fire all the local actions of the child
            ArrayList<Transition> enabledChildTransitions = child.getStateToTransitions().get(currState.getActiveChildState());
            LinkedHashSet<String> localChildTransLabels = child.getLocalActions(syncActions, root);

            for (Transition trans: enabledChildTransitions) {
                if (localChildTransLabels.contains(trans.getLabel())) {
                    GSQState childStepTarget = new GSQState(currState);
                    childStepTarget.setActiveChildState(trans.getTarget());

                    if (!product.isStateMarked(childStepTarget.toString())) {
                        product.addState(childStepTarget.toString());
                        frontier.add(childStepTarget);
                        product.markState(childStepTarget.toString());
                    }

                    product.addTransition(currState.toString(), trans.getLabel(), childStepTarget.toString());

                }
            }

            // fire all the local actions of the root and synchronized ones
            ArrayList<Transition> enabledRootTransitions = root.getStateToTransitions().get(currState.getRootState());
            LinkedHashSet<String> localRootTransLabels = root.getLocalActions(syncActions, children);

            for (Transition rootTrans: enabledRootTransitions) {

                // local transitions of the root
                if (localRootTransLabels.contains(rootTrans.getLabel())) {

                    GSQState rootStepTarget = new GSQState(currState);
                    rootStepTarget.setRootState(rootTrans.getTarget());

                    if (!product.isStateMarked(rootStepTarget.toString())) {
                        product.addState(rootStepTarget.toString());
                        frontier.add(rootStepTarget);
                        product.markState(rootStepTarget.toString());
                    }

                    product.addTransition(currState.toString(), rootTrans.getLabel(), rootStepTarget.toString());

                } else { // synchronized transitions
                    for (Transition childTrans: enabledChildTransitions) { // do not optimize
                        if (childTrans.getLabel().equals(rootTrans.getLabel())) {
                            GSQState jointStepTargetTemplate = new GSQState(currState);

                            // update root and memory (this is common for all targets of the joint action)
                            jointStepTargetTemplate.setRootState(rootTrans.getTarget());
                            jointStepTargetTemplate.updateChildMemory(jointStepTargetTemplate.getActiveChild(), childTrans.getTarget());

                            // wake up a child from memory and register transition
                            for (Automaton nextChild: jointStepTargetTemplate.getChildren()) {
                                GSQState jointStepTarget = new GSQState(jointStepTargetTemplate);
                                String savedState = jointStepTarget.getChildMemory(nextChild);
                                jointStepTarget.setActiveChild(nextChild);
                                jointStepTarget.setActiveChildState(savedState);

                                if (!product.isStateMarked(jointStepTarget.toString())) {
                                    product.addState(jointStepTarget.toString());
                                    frontier.add(jointStepTarget);
                                    product.markState(jointStepTarget.toString());
                                }

                                product.addTransition(currState.toString(), rootTrans.getLabel(), jointStepTarget.toString());

                            }

                        }
                    }
                }

            }

        }

        product.resetStateMarkings();
        return product;
    }

    /**
     * Returns the subset of the children that share a common action with the root and are not equal to the root.
     */
    public static ArrayList<Automaton> discoverChildren(Automaton root, ArrayList<Automaton> children, LinkedHashSet<String> syncActions) {
        ArrayList<Automaton> syncChildren = new ArrayList<>();
        for (Automaton child: children)
            if (child != root && !root.getSyncActions(syncActions, child).isEmpty()) syncChildren.add(child);

        return syncChildren;
    }

    /**
     * Computes general square product. Note - it's up to you to ensure that the structure of the automata net is tree-like.
     * If there are cycles, then this will loop into infinity. It is also assumed that the root is not present in children.
     */
    public static Automaton generalSquareProduct(Automaton root, ArrayList<Automaton> children, LinkedHashSet<String> syncActions, boolean verbose) {
        ArrayList<Automaton> syncChildren = GSQProduct.discoverChildren(root, children, syncActions);
        if (syncChildren.isEmpty()) return root;

        ArrayList<Automaton> reducedChildren = new ArrayList<>();
        for (Automaton child: syncChildren) {
            reducedChildren.add(GSQProduct.generalSquareProduct(child,
                    children.stream().filter(kid -> kid != child).collect(Collectors.toCollection(ArrayList::new)),
                    syncActions,
                    verbose));
        }
        Automaton product = singleLevelProduct(root, reducedChildren, syncActions, verbose);

        product.remapStates();
        return product;
    }

    public static Automaton generalSquareProduct(AutomataNet net, boolean verbose) {
        Automaton root = net.getAutomata().get(0);
        ArrayList<Automaton> children = new ArrayList<>();
        for (int i = 1; i < net.getAutomata().size(); ++i) children.add(net.getAutomata().get(i));

        return generalSquareProduct(root, children, net.getActions(), verbose);
    }

}
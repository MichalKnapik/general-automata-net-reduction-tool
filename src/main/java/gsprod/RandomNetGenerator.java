package gsprod;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Scanner;

public class RandomNetGenerator {

   public static void DFSNetGen(String prefix, int minBranchingFactor, int maxBranchingFactor, int depth,
                         RandomAutomatonGenerator gen, AutomataNet net, Random rng) {

                         int branchingFactor = rng.nextInt(maxBranchingFactor - minBranchingFactor + 1) + minBranchingFactor;
                         LinkedHashSet<String> syncActs = new LinkedHashSet<>();
                         LinkedHashSet<String> syncDownActs = new LinkedHashSet<>();
                         for (int i = 0; i < branchingFactor; ++i) syncDownActs.add(prefix+i);

                         syncActs.add(prefix);
                         if (depth > 0) syncActs.addAll(syncDownActs);
                         Automaton node = gen.generate(syncActs);
                         net.getAutomata().add(node);
                         net.getActions().addAll(syncDownActs);

                         if (depth > 0)
                         for (String deeperPrefix: syncDownActs) DFSNetGen(deeperPrefix, minBranchingFactor, maxBranchingFactor, depth-1, gen, net, rng);
                         }

    public static AutomataNet DFSNetGen(int minBranchingFactor, int maxBranchingFactor, int depth, int minAutoSize, int maxAutoSize) {
        RandomAutomatonGenerator gen = new RandomAutomatonGenerator(minAutoSize, maxAutoSize);
        Random rng = new Random();
        AutomataNet net = new AutomataNet();
        DFSNetGen("a", minBranchingFactor, maxBranchingFactor, depth, gen, net, rng);

        return net;
    }

    public static void main(String[] args) {
        Scanner myInput = new Scanner(System.in);
        int minStates, maxStates, minBranch, maxBranch, depth;
        System.out.println("Min st. size: ");
        minStates = myInput.nextInt();
        System.out.println("Max st. size: ");
        maxStates = myInput.nextInt();
        System.out.println("Min branching factor: ");
        minBranch = myInput.nextInt();
        System.out.println("Max branching factor: ");
        maxBranch = myInput.nextInt();
        System.out.println("Depth: ");
        depth = myInput.nextInt();

        System.out.println(DFSNetGen(minBranch, maxBranch, depth, minStates, maxStates));
    }

}

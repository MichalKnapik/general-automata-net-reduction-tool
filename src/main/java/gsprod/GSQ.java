package gsprod;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;

@Command(name = "gsprod.GSQ", mixinStandardHelpOptions = true, version = "0.1", usageHelpWidth = 200,
        description = "General square product reduction prototype (2021).")
public class GSQ implements Callable<Integer> {

    @Parameters(arity = "0..1", index = "0", description = "The file with names of synchronizing actions. Unused when running random experiments.")
    private String actionFile;

    @Parameters(arity = "0..1", index = "1..*", description = "The file with names of synchronizing actions. Unused when running random experiments.")
    private String[] modelFiles;

    @Option(names = {"-v", "--verbose"}, description = "Verbose: if true then output all models to stdout.")
    boolean verbose;

    @Option(names = {"-o", "--only-dump"}, description = "Only dump the model files, don't reduce (works only with -d).")
    boolean dumpOnly;

    @Option(names = {"-l", "--live-reset"}, description = "Generate live-reset automata, don't reduce (works only with -d).")
    boolean liveReset;

    @Option(names = "-r", arity = "5", hideParamSyntax = true, paramLabel = "minbf maxbf minautsize maxautsize depth",
            description = "Random experiment parameters. A random synchronization tree of a given depth " +
            "is created in such a way that every inner node has a number of children uniformly chosen from [minbf,maxbf] " +
            "and the number of states of each generated automaton is in [minautsize, maxautsize].")
    int[] randomOptions;

    @Option(arity = "1", names = {"-d", "--dump"}, hideParamSyntax = true, paramLabel = "modelFilePrefix",
            description = "Dump: save randomly generated net to files.")
    String dumpFname;

    @Spec CommandSpec spec;

    public Integer call() throws Exception {

        if (actionFile == null && randomOptions == null) {
            throw new ParameterException(spec.commandLine(), "Please provide either input files or random experiment parameters.");
        }

        AutomataNet nr = null;

        if (randomOptions != null) {
            // *** running a random experiment ***
            int minbf = randomOptions[0];
            int maxbf = randomOptions[1];
            int minautsize = randomOptions[2];
            int maxautsize = randomOptions[3];
            int depth = randomOptions[4];

            nr = RandomNetGenerator.DFSNetGen(minbf, maxbf, depth, minautsize, maxautsize, liveReset);
            System.out.println(String.format("** I have generated a random automata network with branching factor in [%d %d], " +
                    "automaton size in [%d %d] and depth %d **", minbf, maxbf, minautsize, maxautsize, depth));
            if (verbose) System.out.println(nr);
            if (dumpFname != null) nr.dumpFile(dumpFname);
            if (dumpOnly) return 0;

        } else {
            // *** reading actions and automata from files ***
            nr = new AutomataNet();

            // running synchronisation on provided files
            if (verbose) System.out.println("Read from " + this.actionFile + " synchronizing actions:");
            nr.readActions(this.actionFile);
            if (verbose) for (String actName : nr.getActions()) System.out.println(actName);

            if (verbose) System.out.println("Reading models.");
            for (String modelFile : this.modelFiles) {
                Automaton model = nr.readAutomaton(modelFile);
                if (verbose) System.out.println("Read from " + modelFile + " " + model);
            }

            // select the first automaton as the root and the the rest as the children
            Automaton root = nr.getAutomata().get(0);
            ArrayList<Automaton> children = new ArrayList<>();
            for (int i = 1; i < this.modelFiles.length - 1; ++i) children.add(nr.getAutomata().get(i));
        }

        // run the reduction
        Automaton product = GSQProduct.generalSquareProduct(nr, verbose);

        if (verbose) {
            System.out.println(">> The computed product is " + product);
        }

        System.out.println("*** Product's stats: ***\n" + "states count: " + product.getStates().size()
                + "\ntransition count: " + product.countTransitions());
        System.out.println("*** Done. ***");

        return 0;                           
    }

    public static void main(String[] args) {
        System.out.println("-=-=- gsprod.GSQ: General Square Product prototype (2021). -=-=-");
        int exitCode = new CommandLine(new GSQ()).execute(args);
        System.exit(exitCode);
   }

}
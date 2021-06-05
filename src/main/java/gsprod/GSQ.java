package gsprod;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "gsprod.GSQ", mixinStandardHelpOptions = true, version = "0.1", description = "General square product reduction prototype (2021).")
public class GSQ implements Callable<Integer> {

    @Parameters(arity = "1", index = "0", description = "The file with names of synchronizing actions.")
    private String actionFile;

    @Parameters(arity = "1", index = "1..*", description = "The file with names of synchronizing actions.")
    private String[] modelFiles;

    @Option(names = "-v", description = "Verbose: if true then will output all models to stdout.")
    boolean verbose;

    public Integer call() throws Exception {
        AutomataNet nr = new AutomataNet();

        if (verbose) System.out.println("Read from " + this.actionFile + " synchronizing actions:");
        nr.readActions(this.actionFile);
        if (verbose) for (String actName: nr.getActions()) System.out.println(actName);

        if (verbose) System.out.println("Reading models.");
        for (String modelFile: this.modelFiles) {
            Automaton model = nr.readAutomaton(modelFile);
            if (verbose) System.out.println("Read from " + modelFile + " " + model);
        }

        // select the first automaton as the root, the rest as the children and run the reduction

        Automaton root = nr.getAutomata().get(0);
        ArrayList<Automaton> children = new ArrayList<>();
        for (int i = 1; i < this.modelFiles.length-1; ++i) children.add(nr.getAutomata().get(i));

        Automaton product = GSQProduct.generalSquareProduct(root, children, nr.getActions(), verbose);

        if (verbose) {
            System.out.println(">> The computed product is " + product);
        }

        System.out.println("*** Product's stats: ***\n" + "states count: " + product.getStates().size()
                + "\ntransition count: " + product.countTransitions());
        System.out.println("*** Done. **");

        return 0;
    }

    public static void main(String[] args) {
        System.out.println("-=-=- gsprod.GSQ: General Square Product prototype (2021). -=-=-");
        int exitCode = new CommandLine(new GSQ()).execute(args);
        System.exit(exitCode);
   }

}

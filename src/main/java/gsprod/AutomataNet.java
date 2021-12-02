package gsprod;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AutomataNet {

    private LinkedHashSet<String> actions;
    private ArrayList<Automaton> automata;
    public LinkedHashSet<String> getActions() {
        return this.actions;
    }
    public ArrayList<Automaton> getAutomata() {
        return this.automata;
    }

    public AutomataNet() {
        this.actions = new LinkedHashSet<>();
        this.automata = new ArrayList<>();
    }

    public void readActions(String fname) {
        try (Reader freader = new FileReader(fname)) {
            StreamTokenizer sttok = this.getStreamTokenizer(freader);
            while (this.fetchToken(sttok) != null) actions.add(sttok.sval);
        } catch (FileNotFoundException ex) {
            System.err.println("File not found.");
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    public Automaton readAutomaton(String fname) {

        ArrayList<String> states = new ArrayList<>();
        ArrayList<Transition> transitions = new ArrayList<>();

        try (Reader freader = new FileReader(fname)) {
            StreamTokenizer sttok = this.getStreamTokenizer(freader);

            if (this.fetchToken(sttok) == null) throw new RuntimeException("Empty model file.");

            // read states
            if (!sttok.sval.equals("states")) throw new RuntimeException("Expected 'states'.");
            while (true) {
                if (this.fetchToken(sttok) == null) throw new RuntimeException("Missing transitions section.");
                if (sttok.sval.equals("transitions")) break;
                states.add(sttok.sval);
            }

            // read transitions
            while (this.fetchToken(sttok) != null) {
                String source = sttok.sval;
                if (this.fetchToken(sttok) == null) throw new RuntimeException("Missing transition label.");
                String transLabel = sttok.sval;
                if (this.fetchToken(sttok) == null) throw new RuntimeException("Missing transition target.");
                String target = sttok.sval;
                transitions.add(new Transition(source, transLabel, target));
            }

        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (RuntimeException rtex) {
            System.err.println(rtex);
        }

        Automaton automaton = new Automaton(states, transitions);
        automaton.removeUnreachable();
        this.getAutomata().add(automaton);

        return automaton;
    }

    private StreamTokenizer getStreamTokenizer(Reader freader) {
        StreamTokenizer sttok = new StreamTokenizer(freader);
        sttok.ordinaryChars('0', '9');
        sttok.wordChars('0', '9');
        sttok.wordChars('_', '_');

        return sttok;
    }

    private String fetchToken(StreamTokenizer sttok) throws IOException {
        while (sttok.nextToken() != StreamTokenizer.TT_EOF) {
            if (sttok.sval == null) continue;
            return sttok.sval;
        }

        return null;
    }

    public String toString() {
        String str = "** A network of automata:";
        for (Automaton automaton: this.getAutomata()) str += "\n>>" + automaton.toString();
        str += "\n** with registered sync actions: " + this.getActions().stream().reduce("",
                (p, t) -> p + (p.length()>0 ? ", " : "") + t);

        return str+".";
    }

    /**
     * Save the network to files: namePrefixSync, namePrefixM1, namePrefixM2, ...
     */
    public void dumpFile(String namePrefix) {
        try {
            BufferedWriter actWriter = new BufferedWriter(new FileWriter(namePrefix + "Sync"));
            for (String action: this.getActions()) actWriter.write(action+"\n");
            actWriter.close();

            for (Automaton automaton: this.getAutomata()) {
                BufferedWriter autoWriter = new BufferedWriter(new FileWriter(namePrefix + "M" + automaton.getName()));
                autoWriter.write("states\n");
                for (String state: automaton.getStates()) autoWriter.write(state + "\n");
                autoWriter.write("transitions\n");

                ArrayList<Transition> transitions = new ArrayList<>();
                for (String state: automaton.getStateToTransitions().keySet()) {
                    transitions.addAll(automaton.getStateToTransitions().get(state));
                }

                // don't change commas or anything below or external tool won't parse it
                for (Transition trans: transitions)
                    autoWriter.write("(" + trans.getSource() + ", " + trans.getLabel() + " ," + trans.getTarget()+")\n");

                autoWriter.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

}
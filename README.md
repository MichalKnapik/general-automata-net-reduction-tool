# A prototype tool for computing general square products of automata

It's a simple research tool that implements a version of this reduction method:
[live-reset tree reduction tool](https://github.com/MichalKnapik/automata-net-reduction-tool)
but does not assume that the automata are special.

The tool can also generate

## Compiling and running

To compile run *mvn package* (which means that you need Maven and Java). You should get a jar with all dependencies:
*target/gsq-jar-with-dependencies.jar*. Copy it where you want and un *java -jar gsq-jar-with-dependencies.jar* to get the usage description: 

```
-=-=- gsprod.GSQ: General Square Product prototype (2021). -=-=-
Please provide either input files or random experiment parameters.
Usage: gsprod.GSQ [-hlovV] [-d=modelFilePrefix] [-r=minbf maxbf minautsize maxautsize depth]... [<actionFile>] [<modelFiles>...]
General square product reduction prototype (2021).
      [<actionFile>]      The file with names of synchronizing actions. Unused when running random experiments.
      [<modelFiles>...]   The file with names of synchronizing actions. Unused when running random experiments.
  -d, --dump=modelFilePrefix
                          Dump: save randomly generated net to files.
  -h, --help              Show this help message and exit.
  -l, --live-reset        Generate live-reset automata, don't reduce (works only with -d).
  -o, --only-dump         Only dump the model files, don't reduce (works only with -d).
  -r=minbf maxbf minautsize maxautsize depth
                          Random experiment parameters. A random synchronization tree of a given depth is created in such a way that every inner node has a number of children uniformly chosen from
                            [minbf,maxbf] and the number of states of each generated automaton is in [minautsize, maxautsize].
  -v, --verbose           Verbose: if true then output all models to stdout.
  -V, --version           Print version information and exit.
  ```

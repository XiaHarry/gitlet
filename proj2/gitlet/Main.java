package gitlet;

import java.io.File;
import java.io.Serializable;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Harry Xia (for real this time)
 */
public class Main implements Serializable {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args == null || args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (args[0].equals("init") && args.length == 1) {
            Repository repo = new Repository(args);
        } else if (!new File(".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
        } else if (args[0].equals("add")) {
            Repository.add(args);
        } else if (args[0].equals("commit")) {
            Repository.commit(args);
        } else if (args[0].equals("log")) {
            Repository.log();
        } else if (args[0].equals("checkout")) {
            Repository.checkout(args);
        } else if (args[0].equals("rm")) {
            Repository.remove(args);
        } else if (args[0].equals("global-log")) {
            Repository.globalLog();
        } else if (args[0].equals("find")) {
            Repository.find(args);
        } else if (args[0].equals("status")) {
            Repository.status();
        } else if (args[0].equals("branch")) {
            Repository.branch(args);
        } else if (args[0].equals("rm-branch")) {
            Repository.removeBranch(args);
        } else if (args[0].equals("reset")) {
            Repository.reset(args);
        } else if (args[0].equals("merge")) {
            Repository.merge(args);
        } else {
            System.out.println("No command with that name exists.");
        }
    }
}

package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Harry Xia
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* TODO: fill in the rest of this class. */
    public static final ArrayList<Commit> commitList = new ArrayList<Commit>();


    /** Init */
    public Repository(String[] args) {
        if (new File(".gitlet").exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        File file = new File(".gitlet");
        File GITLET_DIR = join(CWD, ".gitlet");
        // Set up persistence
        file.mkdir();
        ////It will have a single branch: Master,
        Commit initCommit = new Commit("initial commit", null, "master");
        HashMap<String, Commit> newCommits = new HashMap<String, Commit>();
        //Master will be the current branch.
        newCommits.put("master", initCommit);
        //Use Helper to serialize, think of another way to fix not passing the init autograder
        Helper initHelper = new Helper("master");
        //Use array to store commitList, fix runtime issue
        ArrayList<Commit> commitList = new ArrayList<Commit>();
        commitList.add(initCommit);
        Repository.serialize(initHelper, ".gitlet/staging");
        Repository.serialize(newCommits, ".gitlet/hash");
        Repository.serialize(commitList, ".gitlet/list");
    }

    /** Serialize object into file. @Stack overflow code inspired */
    static void serialize(Object object, String path) {
        Object objectSeriable = object;
        File outFile = new File(path);
        //Stackoverflow
        try {
            FileOutputStream fileOut = new FileOutputStream(outFile);
            ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
            objOut.writeObject(objectSeriable);
            objOut.close();
        } catch (IOException exception) {
            System.out.println(exception);
        }
    }

    /** Deserialize a file into a object. @Stack overflow code inspired */
    static Object deserialize(String path) {
        Object object;
        File inFile = new File(path);
        //Stackoverflow
        try {
            FileInputStream fileIn = new FileInputStream(inFile);
            ObjectInputStream input = new ObjectInputStream(fileIn);
            object = input.readObject();
            input.close();
            return object;
        } catch (IOException | ClassNotFoundException exception) {
            return exception;
        }
    }

    /** Add file */
    public static void add(String[] args) {
        if (args.length == 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        //Add a file, staging the file for addition.
        // Staging an already-staged file overwrites the previous entry in the staging area
        // with the new contents.
        File file = new File(args[1]);
        Object objectToStage = Repository.deserialize(".gitlet/staging");
        Object objectToHash = Repository.deserialize(".gitlet/hash");

        //use Helper method, use casting
        Helper help = (Helper) objectToStage;
        HashMap<String, Commit> commits = (HashMap<String, Commit>) objectToHash;

        //Check for file existence
        if (!file.isFile()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        if (help.getGiven() != null && help.getGiven().contains(args[1])) {
            Commit newCommit = commits.get(help.getNewChild());
            while (newCommit != null && !newCommit.getFiles().contains(args[1])) {
                newCommit = newCommit.getParent();
            }
            if (Repository.helperCompare(Paths.get(args[1]), Paths.get(".gitlet/" + newCommit.getID() + "/" + args[1]))) {
                return;
            }
        }
        help.getFiles().add(args[1]);
        if (help.getDeleted() != null && help.getDeleted().contains(args[1])) {
            help.getDeleted().remove(args[1]);
            help.getFiles().remove(args[1]);
        }
        Repository.serialize(help, ".gitlet/staging");
    }

    public static boolean helperCompare(Path p1, Path p2) {
        //Stack overflow
        try {
            return Arrays.equals(Files.readAllBytes(p1),
                    Files.readAllBytes(p2));
        } catch (IOException exception) {
            return false;
        }
    }

    /** Commit method*/
    public static void commit(String[] args) {
        if (commitError(args)) {
            return;
        }

        //start committing
        Object objectToStage = Repository.deserialize(".gitlet/staging");
        Object objectToHash = Repository.deserialize(".gitlet/hash");
        Helper help = (Helper) objectToStage;
        HashMap<String, Commit> commits = (HashMap<String, Commit>) objectToHash;

        //Check for size
        if (help.getFiles().size() == 0) {
            if (help.getDeleted().size() != 0) {
                help.setDeleted(new HashSet<String>());
                Repository.serialize(help, ".gitlet/staging");
            } else {
                System.out.println("No changes added to the commit.");
                return;
            }
        }
        Commit newCommits2 = new Commit(args[1], (Commit) commits.get(help.getNewChild()), help.getNewChild());
        newCommits2.commitEverything(help);
        ArrayList<Commit> listTocommit = (ArrayList<Commit>) Repository.deserialize(".gitlet/list");
        listTocommit.add(newCommits2);
        Repository.serialize(listTocommit, ".gitlet/list");
        commits.put(help.getNewChild(), newCommits2);
        String s = ".gitlet/" + newCommits2.getID();
        File directory = new File(s);
        //set up persistence
        directory.mkdir();
        //Environment set up, inspired by TA
        for (String file : help.getFiles()) {
            help.getGiven().add(file);
            // create pathname
            String add = s + "/" + file;
            //create path
            Path path = Paths.get(file);
            Path newPath = Paths.get(add);
            File newFile = new File(add);
            newFile.getParentFile().mkdirs();

            copy(path, newPath);
        }
        Repository.serialize(commits, ".gitlet/hash");
        help.clear();
        Repository.serialize(help, ".gitlet/staging");
    }

    /** Commit error helper function*/
    public static boolean commitError(String[] args) {
        if (args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            return true;
        }
        return false;
    }

    public static void log() {

        //deserialize staging area first
        Helper h = (Helper) Repository.deserialize(".gitlet/staging");
        HashMap<String, Commit> commits = (HashMap<String, Commit>) Repository.deserialize(".gitlet/hash");
        Commit initCommit = commits.get(h.getNewChild());

        //Display the commit id, the time the commit was made, and the commit message.
        while (initCommit != null) {
            initCommit.print();

            //Get parent!!!
            initCommit = initCommit.getParent();
        }
    }

    /** Copy Help, catch code from @stack overflow*/
    public static void copy(Path path, Path p) {
        try {
            Files.copy(path, p, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            System.exit(0);
        }
    }

    /** Checkout method */
    public static void checkout(String[] args) {
        if (args.length == 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }

        //Set up the environment
        String firstArg = args[1];
        Helper help = (Helper) Repository.deserialize(".gitlet/staging");
        HashMap<String, Commit> myCommits = (HashMap<String, Commit>) Repository.deserialize(".gitlet/hash");

        //Error condition?
        if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
            }
            String newArg = firstArg;
            firstArg = args[3];

            //Branching
            for (String s : myCommits.keySet()) {
                Commit newCommit = myCommits.get(s);
                String currBranch = newCommit.getChild();

                //checkout every commit that's not null
                while (newCommit != null && newCommit.getChild().equals(currBranch)) {
                    if (newCommit.getID().startsWith(newArg, 0) || newCommit.getID().equals(newArg)) {
                        newArg = newCommit.getID();

                        //Check for error condition
                        if (!newCommit.getGiven().contains(firstArg) && !newCommit.getFiles().contains(firstArg)) {
                            System.out.println("File does not exist in that commit.");
                            return;
                        }
                        if (newCommit.getFiles().contains(firstArg)) {
                            String newerString = ".gitlet/" + newArg + "/" + firstArg;
                            Path branchingString = Paths.get(firstArg);
                            Path path = Paths.get(newerString);
                            copy(path, branchingString);
                            return;
                        }
                        //How do I fix this part?
                        while (!newCommit.getFiles().contains(firstArg)) {
                            newCommit = newCommit.getParent();
                        }
                        String newerString = ".gitlet/" + newCommit.getID() + "/" + firstArg;
                        Path nextPath = Paths.get(firstArg);
                        Path path = Paths.get(newerString);
                        copy(path, nextPath);
                    }
                    newCommit = newCommit.getParent();
                }
            }
            //failure cases
            String failureCase1 = "No commit with that id exists.";
            System.out.println(failureCase1);
        } else if (firstArg.equals(help.getNewChild())) {
            String failureCase2 = "No need to checkout the current branch.";
            System.out.println(failureCase2);
            System.exit(0);
        } else {
            //After the commit command
            //the new commit is added as a new node in the commit tree.
            if (args.length == 3) {
                if (!args[1].equals("--")) {
                    System.out.println("Incorrect operands.");
                } else {
                    firstArg = args[2];
                }
            }
            for (String b : myCommits.keySet()) {
                if (b.equals(firstArg)) {
                    Commit newCommit = myCommits.get(help.getNewChild());
                    // Returns a list of the names of all plain files in the directory DIR
                    for (String file : Utils.plainFilenamesIn(".")) {
                        //set up file length
                        int length = file.length();
                        //Stackoverflow substring function
                        if (file.substring(length - 4, length).equals(".txt")) {
                            if (!newCommit.getFiles().contains(file)) {
                                if (!newCommit.getGiven().contains(file) && !help.getFiles().contains(file)) {
                                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                                    return;
                                }
                            }
                        }
                    }
                    //Set up environment again
                    help.setNewChild(firstArg);
                    help.superClear();
                    help.setGiven(new HashSet<String>());
                    Commit Head = myCommits.get(firstArg);
                    //set up the given
                    for (String s : Head.getGiven()) {
                        help.getGiven().add(s);
                    }
                    // deleted next
                    for (String s : Head.getDeleted()) {
                        help.getDeleted().add(s);
                    }
                    //then get the files
                    for (String s : Head.getFiles()) {
                        help.getGiven().add(s);
                    }
                    Repository.serialize(help, ".gitlet/staging");
                    for (String file : Utils.plainFilenamesIn(".")) {
                        int length = file.length();
                        if (file.substring(length - 4, length).equals(".txt")) {
                            if (!Head.getFiles().contains(file)) {
                                File f = new File(file);
                                //Stack overflow restrictedDelete: Deletes FILE if it exists and is not a directory.
                                Utils.restrictedDelete(f);
                            }
                        }
                    }
                    for (String f : Head.getFiles()) {
                        String newS = ".gitlet/" + Head.getID() + "/" + f;
                        Path newPath = Paths.get(f);
                        Path path = Paths.get(newS);
                        copy(path, newPath);
                    }
                    for (String f : Head.getGiven()) {
                        Commit v = Head;
                        while (!v.getFiles().contains(f)) {
                            v = v.getParent();
                        }
                        String newerS = ".gitlet/" + v.getID() + "/" + f;
                        Path newPath = Paths.get(f);
                        Path path = Paths.get(newerS);
                        copy(path, newPath);
                    }
                    return;
                }
            }
            //The staging area is cleared after a commit.
            Commit newCommit = myCommits.get(help.getNewChild());
            if (help.getGiven().contains(firstArg)) {
                while (!newCommit.getFiles().contains(firstArg)) {
                    newCommit = newCommit.getParent();
                }
                //New string and path? Consider deleting?
                String newS = ".gitlet/" + newCommit.getID() + "/" + firstArg;
                Path newP = Paths.get(firstArg);
                Path path = Paths.get(newS);
                copy(path, newP);
            } else {
                System.out.println("No such branch exists.");
            }
        }
    }

    public static void remove(String[] args) {
        if (args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Helper help = (Helper) Repository.deserialize(".gitlet/staging");
        Object hashObject = Repository.deserialize(".gitlet/hash");
        HashMap<String, Commit> commits = (HashMap<String, Commit>) hashObject;
        Commit childCommit = commits.get(help.getNewChild());
        //If the file is neither staged nor tracked by the head commit
        // print the error message
        if (!help.getFiles().contains(args[1]) && !childCommit.getFiles().contains(args[1])) {
            System.out.println("No reason to remove the file.");
        }
        if (help.getFiles().contains(args[1])) {
            help.getFiles().remove(args[1]);
        }
        if (childCommit.getFiles().contains(args[1])) {
            File newFile = new File(args[1]);
            //Deletes FILE if it exists and is not a directory
            // Returns true if FILE was deleted, and false otherwise.
            if (newFile.isFile()) {
                Utils.restrictedDelete(newFile);
            }
            childCommit.getFiles().remove(args[1]);
            help.getDeleted().add(args[1]);
        }
        if (help.getGiven().contains(args[1])) {
            help.getGiven().remove(args[1]);
        }
        Repository.serialize(help, ".gitlet/staging");
    }

    public static void globalLog() {
        ArrayList<Commit> commitList = (ArrayList<Commit>) Repository.deserialize(".gitlet/list");
        for (Commit commits : commitList) {
            commits.print();
        }
    }
//Prints out the ids of all commits that have the given commit message,
// one per line. If there are multiple such commits,
// it prints the ids out on separate lines.
    public static void find(String[] args) {
        if (args.length == 1) {
            System.out.println("Did not enter enough arguments");
            System.exit(0);
        }
        boolean b = false;
        String s = args[1];
        Object myCommit = Repository.deserialize(".gitlet/list");
        ArrayList<Commit> commitList = (ArrayList<Commit>) myCommit;
        for (Commit c : commitList) {
            if (c.getMessage().equals(s)) {
                System.out.println(c.getID());
                b = true;
            }
        }
        if (b == false) {
            System.out.println("Found no commit with that message");
        }
    }

    public static void status() {
        System.out.println("=== Branches ===");
        Object stage = Repository.deserialize(".gitlet/staging");
        Helper h = (Helper) stage;
        Object hash = Repository.deserialize(".gitlet/hash");
        HashMap<String, Commit> commits = (HashMap<String, Commit>) hash;
        ArrayList<String> myList = new ArrayList<String>();
        for (String s : commits.keySet()) {
            myList.add(s);
        }
        java.util.Collections.sort(myList);
        for (String s : myList) {
            if (s.equals(h.getNewChild())) {
                System.out.println("*" + s);
            } else {
                System.out.println(s);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String file : h.getFiles()) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String file : h.getDeleted()) {
            System.out.println(file);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    //Creates a new branch with the given name,
    // and points it at the current head commit.
    // A branch is nothing more than a name for a reference
    // (a SHA-1 identifier) to a commit node.
    public static void branch(String[] args) {
        if (args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        String s = args[1];
        Helper help = (Helper) Repository.deserialize(".gitlet/staging");
        HashMap<String, Commit> commits = (HashMap<String, Commit>) Repository.deserialize(".gitlet/hash");
        if (commits.containsKey(s)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Commit myCommit = commits.get(help.getNewChild());
        commits.put(s, myCommit);
        Repository.serialize(commits, ".gitlet/hash");
    }

    /** Remove the pointer to the child.*/
    public static void removeBranch(String[] args) {
        Repository.commitError(args);
        String s = args[1];
        Helper help = (Helper) Repository.deserialize(".gitlet/staging");
        HashMap<String, Commit> commits = (HashMap<String, Commit>) Repository.deserialize(".gitlet/hash");
        if (!commits.containsKey(s)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (help.getNewChild().equals(s)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        commits.remove(s);
        Repository.serialize(commits, ".gitlet/hash");
    }

    /** Checks out all the files tracked by the given commit. */
    public static void reset(String[] args) {
        if (args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Object stage = Repository.deserialize(("gitlet/staging"));
        System.out.println("No commit with that id exists.");
    }

    //Merge files
    public static void merge(String[] args) {
        if (args[1].equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        Helper help = (Helper) Repository.deserialize(".gitlet/staging");
        HashMap<String, Commit> commits1 = (HashMap<String, Commit>) Repository.deserialize(".gitlet/staging");
        HashMap<String, Commit> commits = (HashMap<String, Commit>) Repository.deserialize(".gitlet/hash");
        HashMap<String, Commit> myCommits = (HashMap<String, Commit>) Repository.deserialize(".gitlet/hash");
        Commit newCommit = myCommits.get(help.getNewChild());
        if (!newCommit.getGiven().contains(Utils.plainFilenamesIn(".")) && !help.getFiles().contains(Utils.plainFilenamesIn("."))) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
        }
        if (!commits1.isEmpty() || !commits.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!commits.containsKey(args[1])) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (args[1].equals(help.getNewChild())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        Commit first = commits.get(args[1]);
        Commit c = commits.get(help.getNewChild());
        HashSet<String> mergeThenCommit = new HashSet<String>();
        Commit variable = first;
        while (variable != null) {
            mergeThenCommit.add(variable.getID());
            variable = variable.getParent();
        }
        Commit merged = c;
        while (!mergeThenCommit.contains(merged.getID())) {
            merged = merged.getParent();
        }
        HashSet<String> createdThenCombined = new HashSet<String>();
        for (String file : first.getDeleted()) {
            createdThenCombined.remove(file);
        }
        HashSet<String> myCreatedThenCombined = new HashSet<String>();
        for (String file : c.getDeleted()) {
            myCreatedThenCombined.remove(file);
        }
        for (String file : createdThenCombined) {
            variable = first;
            while (!variable.getFiles().contains(file)) {
                variable = variable.getParent();
            } if (!myCreatedThenCombined.contains(file)) {
                String s = ".gitlet/" + variable.getID() + "/" + file;
                Path newP = Paths.get(file);
                Path path = Paths.get(s);
                Repository.copy(path, newP);
                System.out.println("Given branch is an ancestor of the current branch.");
            } else {
                String s = ".gitlet/" + variable.getID() + "/" + file;
                String s1 = file + ".conflicted";
                Path newP = Paths.get(s1);
                Path path = Paths.get(s);
                Repository.copy(path, newP);
                System.out.println("Current branch fast-forwarded");
            }
        }
    }
}

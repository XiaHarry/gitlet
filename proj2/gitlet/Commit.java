package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashSet;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Harry Xia
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String child;
    private Commit parent;
    private String time;

    public HashSet<String> getDeleted() {
        return removed;
    }

    public HashSet<String> getFiles() {
        return files;
    }

    public HashSet<String> getGiven() {
        return givenFile;
    }

    public String getID() {
        return ID;
    }

    public Commit getParent() {
        return parent;
    }

    public String getChild() {
        return child;
    }

    //set up ID
    /** The string ID. **/
    private String ID;
    /** The files commit */
    private HashSet<String> files = new HashSet<String>();

    /** The deleted files. */
    private HashSet<String> removed = new HashSet<String>();

    /** Given files */
    private HashSet<String> givenFile = new HashSet<String>();

    public Commit(String m, Commit parent, String child) {
        message = m;
        this.parent = parent;
        this.child = child;
        SimpleDateFormat simpleDate = new SimpleDateFormat("EEE MMM dd hh:mm:ss YYYY");
        Date date = new Date();
        time = simpleDate.format(date) + " -0800";
        ID = Utils.sha1(message, time);
    }


    //print function
    public void print() {
        System.out.println("===");
        String s = "commit " + ID;
        System.out.println(s);
        System.out.println("Date: " + time);
        System.out.println(message);
        System.out.println();
    }
    public void commitEverything(Helper h) {
        if (h.getDeleted() != null) {
            for (String file : h.getFiles()) {
                if (!files.contains(file)) {
                    givenFile.add(file);
                }
            }
        }
        if (h.getFiles() != null) {
            files.addAll(h.getFiles());
        }
        if (h.getGiven() != null) {
            removed.addAll(h.getDeleted());
        }
    }

    public String getMessage() {
        return message;
    }
}

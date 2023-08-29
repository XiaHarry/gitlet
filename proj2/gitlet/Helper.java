package gitlet;

import java.io.Serializable;
import java.util.HashSet;

/** @author Harry
 * * TA's recommendation of a Helper Class*/

public class Helper implements Serializable {

    private String newChild;

    private HashSet<String> files = new HashSet<String>();

    private HashSet<String> delete = new HashSet<String>();

    private HashSet<String> given = new HashSet<String>();

    public void setNewChild(String child) {
        this.newChild = child;
    }

    public Helper(String child) {
        setNewChild(child);
    }

    public void setDeleted(HashSet<String> delete) {
        this.delete = delete;
    }

    public void setFiles(HashSet<String> files) {
        this.files = files;
    }

    public void setGiven(HashSet<String> given) {
        this.given = given;
    }

    public String getNewChild() {
        return newChild;
    }

    public HashSet<String> getDeleted() {
        return delete;
    }

    public HashSet<String> getFiles() {
        return files;
    }

    public HashSet<String> getGiven() {
        return given;
    }

    /** Clear stage.*/
    public void clear() {
        setFiles(new HashSet<String>());
    }

    /** Clear given and deleted files. */
    public void superClear() {
        setGiven(new HashSet<String>());
        setFiles(new HashSet<String>());
        setDeleted(new HashSet<String>());
    }
}
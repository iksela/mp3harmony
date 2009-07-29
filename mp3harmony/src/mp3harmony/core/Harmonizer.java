package mp3harmony.core;

import java.io.File;

public class Harmonizer {

    private File root;
    private LogEventListener listener;
    private MightyIndex index;

    public Harmonizer(File root, LogEventListener listener) {
        this.root = root;
        this.listener = listener;
        this.index = new MightyIndex(listener);
    }

    public void buildIndex() {
        listener.logIt("Building index...");
        recursiveIndexBuilder(root);
    }

    public void recursiveIndexBuilder(File currentRoot) {
        listener.statusIt(currentRoot.getAbsolutePath().substring(root.getAbsolutePath().length()));
        for (File current : currentRoot.listFiles()) {
            if (current.isDirectory()) {
                recursiveIndexBuilder(current);
            } else if (current.getName().toLowerCase().endsWith("mp3")) {
                index.createEntry(current);
            }
        }
    }

    public int analyze(int threshold) {
        listener.logIt("Analyzing with threshold = " + threshold + " ...");
        return index.analyze(threshold);
    }

    public void correct() {
        listener.logIt("Applying changes...");
        index.makeChanges();
    }
}

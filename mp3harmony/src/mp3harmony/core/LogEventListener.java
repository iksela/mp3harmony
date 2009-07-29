package mp3harmony.core;

import java.util.EventListener;

public interface LogEventListener extends EventListener {

    void logIt(String message);
    void statusIt(String message);
}
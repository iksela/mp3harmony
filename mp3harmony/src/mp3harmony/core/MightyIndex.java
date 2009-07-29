package mp3harmony.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

public class MightyIndex {

    private Map<String, List<File>> artists;
    private Map<File, Map<Integer, String>> changes;
    private LogEventListener listener;
    public static final Integer ID_ARTIST = 1;
    public static final Integer ID_ALBUM = 2;

    public MightyIndex(LogEventListener listener) {
        this.listener = listener;
        artists = new HashMap<String, List<File>>();
        changes = new HashMap<File, Map<Integer, String>>();
    }

    public void createEntry(File mp3file) {
        try {
            MP3File f = (MP3File) AudioFileIO.read(mp3file);
            Tag tag = f.getTag();
            String artist = tag.getFirstArtist();
            if (artists.get(artist) == null) {
                List locations = new ArrayList<File>();
                locations.add(mp3file);
                artists.put(artist, locations);
            } else {
                artists.get(artist).add(mp3file);
            }
        } catch (CannotReadException ex) {
            Logger.getLogger(MightyIndex.class.getName()).log(Level.SEVERE, null, ex);
            listener.logIt(ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(MightyIndex.class.getName()).log(Level.SEVERE, null, ex);
            listener.logIt(ex.getMessage());
        } catch (TagException ex) {
            Logger.getLogger(MightyIndex.class.getName()).log(Level.SEVERE, null, ex);
            listener.logIt(ex.getMessage());
        } catch (ReadOnlyFileException ex) {
            Logger.getLogger(MightyIndex.class.getName()).log(Level.SEVERE, null, ex);
            listener.logIt(ex.getMessage());
        } catch (InvalidAudioFrameException ex) {
            Logger.getLogger(MightyIndex.class.getName()).log(Level.SEVERE, null, ex);
            listener.logIt(ex.getMessage());
        }
    }

    public void makeChanges() {
        for (File changeMe:changes.keySet()) {
            try {
                MP3File f = (MP3File) AudioFileIO.read(changeMe);
                Tag tag = f.getTag();
                for (Integer id:changes.get(changeMe).keySet()) {
                    if (id == ID_ALBUM) {
                        tag.setAlbum(changes.get(changeMe).get(id));
                    }
                    else if (id == ID_ARTIST) {
                        tag.setArtist(changes.get(changeMe).get(id));
                    }
                }
                f.commit();
            } catch (CannotWriteException ex) {
                Logger.getLogger(MightyIndex.class.getName()).log(Level.SEVERE, null, ex);
                listener.logIt(ex.getMessage());
            } catch (CannotReadException ex) {
                Logger.getLogger(MightyIndex.class.getName()).log(Level.SEVERE, null, ex);
                listener.logIt(ex.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(MightyIndex.class.getName()).log(Level.SEVERE, null, ex);
                listener.logIt(ex.getMessage());
            } catch (TagException ex) {
                Logger.getLogger(MightyIndex.class.getName()).log(Level.SEVERE, null, ex);
                listener.logIt(ex.getMessage());
            } catch (ReadOnlyFileException ex) {
                Logger.getLogger(MightyIndex.class.getName()).log(Level.SEVERE, null, ex);
                listener.logIt(ex.getMessage());
            } catch (InvalidAudioFrameException ex) {
                Logger.getLogger(MightyIndex.class.getName()).log(Level.SEVERE, null, ex);
                listener.logIt(ex.getMessage());
            }
        }
    }

    private void findBestMatch(Integer idToTest, String compareMe, String compareTo) {
        int compareMePop = mapFromID(idToTest).get(compareMe).size();
        int compareToPop = mapFromID(idToTest).get(compareTo).size();

        if (compareMePop >= compareToPop) {
            //listener.logIt(compareTo + " --> " + compareMe);
            markForChange(idToTest, compareTo, compareMe);
        } else {
            //listener.logIt(compareMe + " --> " + compareTo);
            markForChange(idToTest, compareMe, compareTo);
        }
    }

    private void markForChange(Integer id, String from, String to) {
        for (File toChange : mapFromID(id).get(from)) {
            if (changes.containsKey(toChange)) {
                changes.get(toChange).put(id, to);
            } else {
                Map<Integer, String> thisChange = new HashMap<Integer, String>(1);
                thisChange.put(id, to);
                changes.put(toChange, thisChange);
            }
        }
    }

    private void levensthein(int threshold, Integer idToTest) {
        List<String> beenThereDoneThat = new ArrayList<String>();

        for (String compareMe : mapFromID(idToTest).keySet()) {
            beenThereDoneThat.add(compareMe);
            for (String compareTo : mapFromID(idToTest).keySet()) {
                if (!beenThereDoneThat.contains(compareTo)) {
                    int lev = StringUtils.getLevenshteinDistance(compareMe, compareTo);
                    if ((lev != 0) && (lev <= threshold) && (compareMe.length() > threshold)) {
                        findBestMatch(idToTest, compareMe, compareTo);
                    }
                }
            }
        }
    }

    public int analyze(int threshold) {
        levensthein(threshold, ID_ARTIST);
        //levensthein(threshold, ID_ALBUM);

        for (File toChange : changes.keySet()) {
            for (Integer id:changes.get(toChange).keySet()) {
                listener.logIt("["+toChange.getName()+"] New value for "+getFieldFromID(id)+" --> "+changes.get(toChange).get(id));
            }
        }

        return changes.size();
    }

    public Map<String, List<File>> mapFromID(Integer id) {
        if (id == ID_ALBUM) {
            return null;
        }
        if (id == ID_ARTIST) {
            return artists;
        }
        return null;
    }

    public static String getFieldFromID(Integer id) {
        if (id == ID_ALBUM) {
            return "ALBUM";
        }
        if (id == ID_ARTIST) {
            return "ARTIST";
        }
        return null;
    }
}

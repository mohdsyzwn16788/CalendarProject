import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileManager {
    private static final String EVENT_FILE = "event.csv";
    private static final String RECUR_FILE = "recurrent.csv";

    // Ensure files exist on startup
    public static void initializeFiles() {
        createFileIfNotExists(EVENT_FILE);
        createFileIfNotExists(RECUR_FILE);
    }

    private static void createFileIfNotExists(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            System.out.println("Error initializing file: " + e.getMessage());
        }
    }

    // --- READ OPERATIONS ---
    public static List<Event> loadEvents() {
        List<Event> events = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(EVENT_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Event e = Event.fromCSV(line);
                    if (e != null) events.add(e);
                }
            }
        } catch (IOException e) { System.out.println("Error reading events: " + e.getMessage()); }
        return events;
    }

    public static List<Recurrence> loadRecurrences() {
        List<Recurrence> recs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(RECUR_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Recurrence r = Recurrence.fromCSV(line);
                    if (r != null) recs.add(r);
                }
            }
        } catch (IOException e) { System.out.println("Error reading recurrences: " + e.getMessage()); }
        return recs;
    }

    // --- WRITE OPERATIONS ---
    public static void saveEvent(Event event) {
        appendToFile(EVENT_FILE, event.toCSV());
    }

    public static void saveRecurrence(Recurrence rec) {
        appendToFile(RECUR_FILE, rec.toCSV());
    }

    private static void appendToFile(String fileName, String content) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) {
            bw.write(content);
            bw.newLine();
        } catch (IOException e) { System.out.println("Write error: " + e.getMessage()); }
    }

    // --- DELETE / UPDATE UTILS ---
    public static void rewriteEvents(List<Event> events) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(EVENT_FILE))) {
            for (Event e : events) {
                bw.write(e.toCSV());
                bw.newLine();
            }
        } catch (IOException e) { System.out.println("Update error: " + e.getMessage()); }
    }

    public static void rewriteRecurrences(List<Recurrence> recs) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RECUR_FILE))) {
            for (Recurrence r : recs) {
                bw.write(r.toCSV());
                bw.newLine();
            }
        } catch (IOException e) { System.out.println("Update error: " + e.getMessage()); }
    }

    // --- HELPER: AUTO ID ---
    public static int getNextId() {
        List<Event> events = loadEvents();
        if (events.isEmpty()) return 1;
        // Find max ID and add 1
        return events.stream().mapToInt(Event::getEventId).max().orElse(0) + 1;
    }

    // --- BACKUP & RESTORE ---
    public static void backup(String destFolder) {
        try {
            // Create folder if it doesn't exist
            Files.createDirectories(Paths.get(destFolder));
            Files.copy(Paths.get(EVENT_FILE), Paths.get(destFolder, "backup_event.csv"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(RECUR_FILE), Paths.get(destFolder, "backup_recurrent.csv"), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Backup successful to: " + destFolder);
        } catch (IOException e) {
            System.out.println("Backup failed: " + e.getMessage());
        }
    }

    public static void restore(String sourceFolder) {
        try {
            Files.copy(Paths.get(sourceFolder, "backup_event.csv"), Paths.get(EVENT_FILE), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(sourceFolder, "backup_recurrent.csv"), Paths.get(RECUR_FILE), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Restore successful!");
        } catch (IOException e) {
            System.out.println("Restore failed (Check if files exist in source): " + e.getMessage());
        }
    }
}
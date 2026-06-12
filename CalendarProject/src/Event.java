import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Event {
    private int eventId;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    // Standard ISO format: 2025-10-05T11:00:00
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Event(int eventId, String title, String description, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    // Getters
    public int getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public LocalDateTime getEndDateTime() { return endDateTime; }

    // Setters for update functionality
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStartDateTime(LocalDateTime startDateTime) { this.startDateTime = startDateTime; }
    public void setEndDateTime(LocalDateTime endDateTime) { this.endDateTime = endDateTime; }

    // Convert object to CSV string
    public String toCSV() {
        return eventId + "," + title + "," + description + "," +
               startDateTime.format(FORMATTER) + "," + endDateTime.format(FORMATTER);
    }

    // Create object from CSV string
    public static Event fromCSV(String csvLine) {
        try {
            String[] parts = csvLine.split(",");
            int id = Integer.parseInt(parts[0]);
            String title = parts[1];
            String desc = parts[2];
            LocalDateTime start = LocalDateTime.parse(parts[3], FORMATTER);
            LocalDateTime end = LocalDateTime.parse(parts[4], FORMATTER);
            return new Event(id, title, desc, start, end);
        } catch (Exception e) {
            // Return null if line is corrupted
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("[ID: %d] %s | %s | Start: %s", 
               eventId, title, description, startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }

    // Check if event occurs on a specific date
    public boolean occursOnDate(java.time.LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        return (startDateTime.isBefore(endOfDay) && endDateTime.isAfter(startOfDay));
    }

    // Check if event overlaps with another event
    public boolean overlapsWith(Event other) {
        return (this.startDateTime.isBefore(other.endDateTime) && 
                this.endDateTime.isAfter(other.startDateTime));
    }
}
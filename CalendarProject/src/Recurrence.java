import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Recurrence {
    private int eventId;
    private String interval; // "DAILY", "WEEKLY", "MONTHLY"
    private int repeatCount; // Number of times to repeat (0 if using endDate)
    private LocalDate endDate; // Date to stop repeating (null if using count)

    public Recurrence(int eventId, String interval, int repeatCount, LocalDate endDate) {
        this.eventId = eventId;
        this.interval = interval.toUpperCase();
        this.repeatCount = repeatCount;
        this.endDate = endDate;
    }

    public int getEventId() { return eventId; }
    public String getInterval() { return interval; }
    public int getRepeatCount() { return repeatCount; }
    public LocalDate getEndDate() { return endDate; }

    // Generate all recurring events based on base event
    public List<Event> generateRecurringEvents(Event baseEvent, int maxEvents) {
        List<Event> recurringEvents = new ArrayList<>();
        int generatedCount = 0;
        
        // Get base event dates
        LocalDateTime currentStart = baseEvent.getStartDateTime();
        LocalDateTime currentEnd = baseEvent.getEndDateTime();
        long durationMinutes = ChronoUnit.MINUTES.between(currentStart, currentEnd);
        
        // Determine how many times to repeat
        int maxRepeats = repeatCount;
        if (repeatCount == 0 && endDate != null) {
            // Calculate max repeats until end date
            if (interval.equals("DAILY")) {
                maxRepeats = (int) ChronoUnit.DAYS.between(currentStart.toLocalDate(), endDate);
            } else if (interval.equals("WEEKLY")) {
                maxRepeats = (int) ChronoUnit.WEEKS.between(currentStart.toLocalDate(), endDate);
            } else if (interval.equals("MONTHLY")) {
                maxRepeats = (int) ChronoUnit.MONTHS.between(currentStart.toLocalDate(), endDate);
            }
        }
        
        // Generate events
        for (int i = 1; i <= maxRepeats && generatedCount < maxEvents; i++) {
            LocalDateTime newStart = currentStart;
            LocalDateTime newEnd = currentEnd;
            
            // Apply interval
            if (interval.equals("DAILY")) {
                newStart = currentStart.plusDays(i);
                newEnd = currentEnd.plusDays(i);
            } else if (interval.equals("WEEKLY")) {
                newStart = currentStart.plusWeeks(i);
                newEnd = currentEnd.plusWeeks(i);
            } else if (interval.equals("MONTHLY")) {
                newStart = currentStart.plusMonths(i);
                newEnd = currentEnd.plusMonths(i);
            }
            
            // Check if we should stop (for endDate)
            if (endDate != null && newStart.toLocalDate().isAfter(endDate)) {
                break;
            }
            
            // Create recurring event
            Event recurringEvent = new Event(
                baseEvent.getEventId() * 1000 + i, // Generate unique ID for recurring instances
                baseEvent.getTitle() + " (Recurring)",
                baseEvent.getDescription(),
                newStart,
                newEnd
            );
            recurringEvents.add(recurringEvent);
            generatedCount++;
        }
        
        return recurringEvents;
    }

    public String toCSV() {
        String endStr = (endDate == null) ? "null" : endDate.toString();
        return eventId + "," + interval + "," + repeatCount + "," + endStr;
    }

    public static Recurrence fromCSV(String line) {
        try {
            String[] parts = line.split(",");
            int id = Integer.parseInt(parts[0]);
            String interval = parts[1];
            int count = Integer.parseInt(parts[2]);
            LocalDate end = parts[3].equals("null") ? null : LocalDate.parse(parts[3]);
            return new Recurrence(id, interval, count, end);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        String info = "Repeats: " + interval;
        if (repeatCount > 0) {
            info += " for " + repeatCount + " times";
        } else if (endDate != null) {
            info += " until " + endDate;
        }
        return " (" + info + ")";
    }
}
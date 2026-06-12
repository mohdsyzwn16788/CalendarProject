import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class SchedulerApp {
    private static Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        FileManager.initializeFiles(); // Ensure CSVs exist
        checkRemindersOnStartup();

        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("          FOP CALENDAR & SCHEDULER APP");
            System.out.println("=".repeat(50));
            System.out.println("1. [View] View Calendar");
            System.out.println("2. [+] Add Event");
            System.out.println("3. [Edit] Edit/Delete Event");
            System.out.println("4. [Search] Search & Filter Events");
            System.out.println("5. [Stats] Event Statistics");
            System.out.println("6. [Backup] Backup / Restore");
            System.out.println("7. [Conflict] Check Event Conflicts");
            System.out.println("8. [Tools] Additional Features");
            System.out.println("9. [Exit] Exit");
            System.out.println("=".repeat(50));
            System.out.print("Select Option (1-9): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": viewCalendarMenu(); break;
                case "2": createEvent(); break;
                case "3": editOrDeleteEvent(); break;
                case "4": searchAndFilterEvents(); break;
                case "5": showEventStatistics(); break;
                case "6": backupRestoreMenu(); break;
                case "7": checkEventConflicts(); break;
                case "8": additionalFeaturesMenu(); break;
                case "9": 
                    System.out.println("\nGoodbye! Your events are saved.");
                    return;
                default: 
                    System.out.println("ERROR: Invalid option. Please try again.");
            }
        }
    }

    // ===== 1. VIEW CALENDAR =====
    private static void viewCalendarMenu() {
        System.out.println("\nVIEW CALENDAR");
        System.out.println("1. Monthly Grid View");
        System.out.println("2. Weekly View");
        System.out.println("3. Daily View");
        System.out.println("4. List All Events");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine();
        
        switch (choice) {
            case "1": showMonthlyView(); break;
            case "2": showWeeklyView(); break;
            case "3": showDailyView(); break;
            case "4": showAllEvents(); break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void showMonthlyView() {
        try {
            System.out.print("Enter Year (e.g., 2025): ");
            int year = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter Month (1-12): ");
            int month = Integer.parseInt(scanner.nextLine());
            
            YearMonth ym = YearMonth.of(year, month);
            int daysInMonth = ym.lengthOfMonth();
            LocalDate firstDay = ym.atDay(1);
            DayOfWeek firstDayOfWeek = firstDay.getDayOfWeek();
            
            // Get all events for this month
            List<Event> allEvents = FileManager.loadEvents();
            Map<Integer, List<String>> dayEvents = new HashMap<>();
            
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = ym.atDay(day);
                List<String> eventsForDay = new ArrayList<>();
                
                for (Event e : allEvents) {
                    if (e.occursOnDate(date)) {
                        eventsForDay.add(e.getTitle());
                    }
                }
                
                if (!eventsForDay.isEmpty()) {
                    dayEvents.put(day, eventsForDay);
                }
            }
            
            // Print calendar header
            System.out.println("\n" + "-".repeat(50));
            System.out.println("          " + ym.getMonth() + " " + year);
            System.out.println("-".repeat(50));
            System.out.println("Sun Mon Tue Wed Thu Fri Sat");
            
            // Print leading spaces
            int startPos = firstDayOfWeek.getValue() % 7; // 0=Sun, 1=Mon, etc.
            for (int i = 0; i < startPos; i++) {
                System.out.print("    ");
            }
            
            // Print days
            for (int day = 1; day <= daysInMonth; day++) {
                if (dayEvents.containsKey(day)) {
                    System.out.printf("%2d* ", day); // Mark day with events
                } else {
                    System.out.printf("%2d  ", day);
                }
                
                if ((day + startPos) % 7 == 0) {
                    System.out.println();
                }
            }
            
            System.out.println("\n\n* Days with events");
            
            // Show events for specific day if requested
            System.out.print("\nEnter day number to view events (or 0 to skip): ");
            int selectedDay = Integer.parseInt(scanner.nextLine());
            if (selectedDay > 0 && selectedDay <= daysInMonth) {
                LocalDate selectedDate = ym.atDay(selectedDay);
                System.out.println("\nEvents on " + selectedDate.format(DATE_FORMATTER) + ":");
                System.out.println("-".repeat(50));
                
                List<Event> eventsOnDate = allEvents.stream()
                    .filter(e -> e.occursOnDate(selectedDate))
                    .sorted(Comparator.comparing(Event::getStartDateTime))
                    .collect(Collectors.toList());
                
                if (eventsOnDate.isEmpty()) {
                    System.out.println("No events scheduled.");
                } else {
                    for (Event e : eventsOnDate) {
                        System.out.printf("  %s (%s - %s)\n",
                            e.getTitle(),
                            e.getStartDateTime().format(TIME_FORMATTER),
                            e.getEndDateTime().format(TIME_FORMATTER));
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void showWeeklyView() {
        try {
            System.out.print("Enter date (yyyy-MM-dd): ");
            LocalDate date = LocalDate.parse(scanner.nextLine());
            
            LocalDate weekStart = date.with(DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);
            
            System.out.println("\nWeek of " + weekStart.format(DATE_FORMATTER) + " to " + weekEnd.format(DATE_FORMATTER));
            System.out.println("-".repeat(60));
            
            List<Event> allEvents = FileManager.loadEvents();
            
            for (int i = 0; i < 7; i++) {
                LocalDate currentDay = weekStart.plusDays(i);
                System.out.println("\n" + currentDay.getDayOfWeek() + " " + currentDay.format(DATE_FORMATTER));
                System.out.println("-".repeat(30));
                
                List<Event> dayEvents = allEvents.stream()
                    .filter(e -> e.occursOnDate(currentDay))
                    .sorted(Comparator.comparing(Event::getStartDateTime))
                    .collect(Collectors.toList());
                
                if (dayEvents.isEmpty()) {
                    System.out.println("No events scheduled.");
                } else {
                    for (Event e : dayEvents) {
                        System.out.printf("  %s - %s | %s\n",
                            e.getStartDateTime().format(TIME_FORMATTER),
                            e.getEndDateTime().format(TIME_FORMATTER),
                            e.getTitle());
                    }
                }
            }
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void showDailyView() {
        try {
            System.out.print("Enter date (yyyy-MM-dd): ");
            LocalDate date = LocalDate.parse(scanner.nextLine());
            
            List<Event> allEvents = FileManager.loadEvents();
            List<Event> dayEvents = allEvents.stream()
                .filter(e -> e.occursOnDate(date))
                .sorted(Comparator.comparing(Event::getStartDateTime))
                .collect(Collectors.toList());
            
            System.out.println("\n" + date.getDayOfWeek() + ", " + date.format(DATE_FORMATTER));
            System.out.println("-".repeat(60));
            
            if (dayEvents.isEmpty()) {
                System.out.println("No events scheduled for today.");
            } else {
                for (Event e : dayEvents) {
                    System.out.printf("\nTime: %s - %s\n", 
                        e.getStartDateTime().format(TIME_FORMATTER),
                        e.getEndDateTime().format(TIME_FORMATTER));
                    System.out.printf("Title: %s\n", e.getTitle());
                    System.out.printf("Description: %s\n", e.getDescription());
                    
                    // Show additional fields if available
                    List<AdditionalField> addFields = FileManager.loadAdditionalFields();
                    for (AdditionalField af : addFields) {
                        if (af.getEventId() == e.getEventId()) {
                            System.out.println("Details: " + af);
                        }
                    }
                    
                    // Check if event is recurring
                    List<Recurrence> recs = FileManager.loadRecurrences();
                    for (Recurrence r : recs) {
                        if (r.getEventId() == e.getEventId()) {
                            System.out.println("Recurrence: " + r);
                        }
                    }
                    System.out.println();
                }
            }
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void showAllEvents() {
        List<Event> events = FileManager.loadEvents();
        List<Recurrence> recs = FileManager.loadRecurrences();
        List<AdditionalField> addFields = FileManager.loadAdditionalFields();
        
        if (events.isEmpty()) {
            System.out.println("\nNo events found.");
            return;
        }
        
        // Sort by date
        events.sort(Comparator.comparing(Event::getStartDateTime));
        
        System.out.println("\nALL EVENTS (" + events.size() + " total)");
        System.out.println("-".repeat(80));
        
        // Group by date
        Map<LocalDate, List<Event>> eventsByDate = events.stream()
            .collect(Collectors.groupingBy(e -> e.getStartDateTime().toLocalDate()));
        
        for (LocalDate date : eventsByDate.keySet().stream().sorted().collect(Collectors.toList())) {
            System.out.println("\n" + date.format(DATE_FORMATTER) + " (" + date.getDayOfWeek() + ")");
            System.out.println("-".repeat(30));
            
            for (Event e : eventsByDate.get(date)) {
                String recurrenceInfo = "";
                for (Recurrence r : recs) {
                    if (r.getEventId() == e.getEventId() || r.getEventId() == e.getEventId() / 1000) {
                        recurrenceInfo = r.toString();
                        break;
                    }
                }
                
                String additionalInfo = "";
                for (AdditionalField af : addFields) {
                    if (af.getEventId() == e.getEventId() || af.getEventId() == e.getEventId() / 1000) {
                        additionalInfo = " | " + af;
                        break;
                    }
                }
                
                System.out.printf("  %s - %s | %s%s%s\n",
                    e.getStartDateTime().format(TIME_FORMATTER),
                    e.getEndDateTime().format(TIME_FORMATTER),
                    e.getTitle(),
                    recurrenceInfo,
                    additionalInfo);
            }
        }
    }

    // ===== 2. CREATE EVENT =====
    private static void createEvent() {
        try {
            System.out.println("\nCREATE NEW EVENT");
            System.out.println("-".repeat(30));
            
            System.out.print("Title: ");
            String title = scanner.nextLine();
            
            System.out.print("Description: ");
            String description = scanner.nextLine();
            
            // Get date
            System.out.print("Date (yyyy-MM-dd) [Today]: ");
            String dateStr = scanner.nextLine();
            LocalDate date;
            if (dateStr.isEmpty()) {
                date = LocalDate.now();
            } else {
                date = LocalDate.parse(dateStr);
            }
            
            // Get start time
            System.out.print("Start Time (HH:mm) [09:00]: ");
            String startTimeStr = scanner.nextLine();
            LocalTime startTime = startTimeStr.isEmpty() ? LocalTime.of(9, 0) : LocalTime.parse(startTimeStr);
            
            // Get end time
            System.out.print("End Time (HH:mm) [10:00]: ");
            String endTimeStr = scanner.nextLine();
            LocalTime endTime = endTimeStr.isEmpty() ? LocalTime.of(10, 0) : LocalTime.parse(endTimeStr);
            
            // Combine date and time
            LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
            LocalDateTime endDateTime = LocalDateTime.of(date, endTime);
            
            // Validate time
            if (endDateTime.isBefore(startDateTime)) {
                System.out.println("ERROR: End time must be after start time!");
                return;
            }
            
            // Generate ID
            int newId = FileManager.getNextId();
            
            // Create event
            Event newEvent = new Event(newId, title, description, startDateTime, endDateTime);
            
            // Check for conflicts
            List<Event> allEvents = FileManager.loadEvents();
            boolean hasConflict = false;
            for (Event e : allEvents) {
                if (newEvent.overlapsWith(e) && e.getEventId() != newId) {
                    System.out.println("WARNING: This event conflicts with:");
                    System.out.println("   " + e.getTitle() + " (" + 
                        e.getStartDateTime().format(TIME_FORMATTER) + " - " + 
                        e.getEndDateTime().format(TIME_FORMATTER) + ")");
                    hasConflict = true;
                }
            }
            
            if (hasConflict) {
                System.out.print("Continue anyway? (y/n): ");
                if (!scanner.nextLine().equalsIgnoreCase("y")) {
                    return;
                }
            }
            
            // Save event
            FileManager.saveEvent(newEvent);
            
            // Recurrence
            System.out.print("\nMake this event recurring? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                System.out.println("\nRECURRENCE SETTINGS");
                System.out.println("1. Daily");
                System.out.println("2. Weekly");
                System.out.println("3. Monthly");
                System.out.print("Choose interval (1-3): ");
                
                String intervalChoice = scanner.nextLine();
                String interval = "";
                switch (intervalChoice) {
                    case "1": interval = "DAILY"; break;
                    case "2": interval = "WEEKLY"; break;
                    case "3": interval = "MONTHLY"; break;
                    default: 
                        System.out.println("Invalid choice. Skipping recurrence.");
                        interval = "";
                }
                
                if (!interval.isEmpty()) {
                    System.out.println("\nStop condition:");
                    System.out.println("1. After N occurrences");
                    System.out.println("2. Until specific date");
                    System.out.print("Choose (1-2): ");
                    
                    String stopChoice = scanner.nextLine();
                    int repeatCount = 0;
                    LocalDate endDate = null;
                    
                    if (stopChoice.equals("1")) {
                        System.out.print("Number of occurrences (including first): ");
                        repeatCount = Integer.parseInt(scanner.nextLine());
                    } else if (stopChoice.equals("2")) {
                        System.out.print("End date (yyyy-MM-dd): ");
                        endDate = LocalDate.parse(scanner.nextLine());
                    }
                    
                    Recurrence rec = new Recurrence(newId, interval, repeatCount, endDate);
                    FileManager.saveRecurrence(rec);
                    System.out.println("OK: Recurrence rule saved.");
                }
            }
            
            // Additional fields
            System.out.print("\nAdd additional fields? (location, category, etc.) (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                System.out.println("\nADDITIONAL FIELDS");
                System.out.print("Location: ");
                String location = scanner.nextLine();
                
                System.out.print("Category (e.g., Work, Personal, Study): ");
                String category = scanner.nextLine();
                
                System.out.print("Priority (LOW, MEDIUM, HIGH): ");
                String priority = scanner.nextLine().toUpperCase();
                
                System.out.print("Attendees (comma-separated): ");
                String attendees = scanner.nextLine();
                
                AdditionalField addField = new AdditionalField(newId, location, category, priority, attendees);
                FileManager.saveAdditionalField(addField);
                System.out.println("OK: Additional fields saved.");
            }
            
            System.out.println("\nSUCCESS: Event created successfully! ID: " + newId);
            
        } catch (DateTimeParseException e) {
            System.out.println("ERROR: Invalid date/time format!");
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // ===== 3. EDIT/DELETE EVENT =====
    private static void editOrDeleteEvent() {
        System.out.println("\nEDIT/DELETE EVENT");
        System.out.println("-".repeat(30));
        
        System.out.print("Enter Event ID to modify: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            
            List<Event> events = FileManager.loadEvents();
            Event target = null;
            
            for (Event e : events) {
                if (e.getEventId() == id) {
                    target = e;
                    break;
                }
            }
            
            if (target == null) {
                System.out.println("ERROR: Event not found.");
                return;
            }
            
            System.out.println("\nFound Event:");
            System.out.println("Title: " + target.getTitle());
            System.out.println("Date: " + target.getStartDateTime().format(DATETIME_FORMATTER));
            System.out.println("Description: " + target.getDescription());
            
            System.out.println("\nChoose action:");
            System.out.println("1. Edit this event");
            System.out.println("2. Delete this event");
            System.out.println("3. Delete this and all recurring instances");
            System.out.print("Choice (1-3): ");
            
            String action = scanner.nextLine();
            
            if (action.equals("1")) {
                editEvent(target);
            } else if (action.equals("2") || action.equals("3")) {
                deleteEvent(target, action.equals("3"));
            } else {
                System.out.println("Invalid choice.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid ID format.");
        }
    }

    private static void editEvent(Event event) {
        try {
            System.out.println("\nEDITING EVENT #" + event.getEventId());
            System.out.println("-".repeat(30));
            
            System.out.print("New Title [" + event.getTitle() + "]: ");
            String newTitle = scanner.nextLine();
            if (!newTitle.isEmpty()) event.setTitle(newTitle);
            
            System.out.print("New Description [" + event.getDescription() + "]: ");
            String newDesc = scanner.nextLine();
            if (!newDesc.isEmpty()) event.setDescription(newDesc);
            
            System.out.print("New Date (yyyy-MM-dd) [" + event.getStartDateTime().toLocalDate() + "]: ");
            String newDateStr = scanner.nextLine();
            LocalDate newDate = newDateStr.isEmpty() ? event.getStartDateTime().toLocalDate() : LocalDate.parse(newDateStr);
            
            System.out.print("New Start Time (HH:mm) [" + event.getStartDateTime().toLocalTime() + "]: ");
            String newStartTimeStr = scanner.nextLine();
            LocalTime newStartTime = newStartTimeStr.isEmpty() ? event.getStartDateTime().toLocalTime() : LocalTime.parse(newStartTimeStr);
            
            System.out.print("New End Time (HH:mm) [" + event.getEndDateTime().toLocalTime() + "]: ");
            String newEndTimeStr = scanner.nextLine();
            LocalTime newEndTime = newEndTimeStr.isEmpty() ? event.getEndDateTime().toLocalTime() : LocalTime.parse(newEndTimeStr);
            
            LocalDateTime newStart = LocalDateTime.of(newDate, newStartTime);
            LocalDateTime newEnd = LocalDateTime.of(newDate, newEndTime);
            
            event.setStartDateTime(newStart);
            event.setEndDateTime(newEnd);
            
            // Update in file
            List<Event> allEvents = FileManager.loadEvents();
            for (int i = 0; i < allEvents.size(); i++) {
                if (allEvents.get(i).getEventId() == event.getEventId()) {
                    allEvents.set(i, event);
                    break;
                }
            }
            
            FileManager.rewriteEvents(allEvents);
            System.out.println("SUCCESS: Event updated successfully!");
            
        } catch (Exception e) {
            System.out.println("ERROR updating event: " + e.getMessage());
        }
    }

    private static void deleteEvent(Event event, boolean deleteAllRecurring) {
        List<Event> allEvents = FileManager.loadEvents();
        List<Event> eventsToRemove = new ArrayList<>();
        
        if (deleteAllRecurring) {
            // Delete all recurring instances (IDs that start with eventId * 1000)
            int baseId = event.getEventId() < 1000 ? event.getEventId() : event.getEventId() / 1000;
            eventsToRemove = allEvents.stream()
                .filter(e -> e.getEventId() == baseId || e.getEventId() / 1000 == baseId)
                .collect(Collectors.toList());
            
            // Also remove recurrence rule
            List<Recurrence> recs = FileManager.loadRecurrences();
            recs.removeIf(r -> r.getEventId() == baseId);
            FileManager.rewriteRecurrences(recs);
            
        } else {
            eventsToRemove.add(event);
        }
        
        allEvents.removeAll(eventsToRemove);
        FileManager.rewriteEvents(allEvents);
        
        System.out.println("SUCCESS: " + eventsToRemove.size() + " event(s) deleted successfully.");
    }

    // ===== 4. SEARCH & FILTER =====
    private static void searchAndFilterEvents() {
        System.out.println("\nSEARCH & FILTER EVENTS");
        System.out.println("-".repeat(30));
        System.out.println("1. Search by keyword");
        System.out.println("2. Filter by date range");
        System.out.println("3. Filter by category");
        System.out.println("4. Filter by priority");
        System.out.println("5. Advanced search");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine();
        
        List<Event> allEvents = FileManager.loadEvents();
        List<Event> filteredEvents = new ArrayList<>(allEvents);
        
        switch (choice) {
            case "1":
                System.out.print("Enter search keyword: ");
                String keyword = scanner.nextLine().toLowerCase();
                filteredEvents = filteredEvents.stream()
                    .filter(e -> e.getTitle().toLowerCase().contains(keyword) ||
                                 e.getDescription().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
                break;
                
            case "2":
                try {
                    System.out.print("Start date (yyyy-MM-dd): ");
                    LocalDate startDate = LocalDate.parse(scanner.nextLine());
                    System.out.print("End date (yyyy-MM-dd): ");
                    LocalDate endDate = LocalDate.parse(scanner.nextLine());
                    
                    filteredEvents = filteredEvents.stream()
                        .filter(e -> !e.getStartDateTime().toLocalDate().isBefore(startDate) &&
                                     !e.getStartDateTime().toLocalDate().isAfter(endDate))
                        .collect(Collectors.toList());
                } catch (Exception e) {
                    System.out.println("Invalid date format.");
                    return;
                }
                break;
                
            case "3":
                List<AdditionalField> addFields = FileManager.loadAdditionalFields();
                System.out.print("Enter category: ");
                String category = scanner.nextLine();
                
                filteredEvents = filteredEvents.stream()
                    .filter(e -> {
                        for (AdditionalField af : addFields) {
                            if (af.getEventId() == e.getEventId() || 
                                af.getEventId() == e.getEventId() / 1000) {
                                return af.getCategory().equalsIgnoreCase(category);
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
                break;
                
            case "4":
                addFields = FileManager.loadAdditionalFields();
                System.out.print("Enter priority (LOW, MEDIUM, HIGH): ");
                String priority = scanner.nextLine().toUpperCase();
                
                filteredEvents = filteredEvents.stream()
                    .filter(e -> {
                        for (AdditionalField af : addFields) {
                            if (af.getEventId() == e.getEventId() || 
                                af.getEventId() == e.getEventId() / 1000) {
                                return af.getPriority().equals(priority);
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
                break;
                
            case "5":
                System.out.println("Enter search criteria (leave blank to skip):");
                
                System.out.print("Keyword: ");
                String kw = scanner.nextLine();
                if (!kw.isEmpty()) {
                    filteredEvents = filteredEvents.stream()
                        .filter(e -> e.getTitle().toLowerCase().contains(kw.toLowerCase()) ||
                                     e.getDescription().toLowerCase().contains(kw.toLowerCase()))
                        .collect(Collectors.toList());
                }
                
                System.out.print("Start date (yyyy-MM-dd): ");
                String sd = scanner.nextLine();
                if (!sd.isEmpty()) {
                    LocalDate startDate = LocalDate.parse(sd);
                    filteredEvents = filteredEvents.stream()
                        .filter(e -> !e.getStartDateTime().toLocalDate().isBefore(startDate))
                        .collect(Collectors.toList());
                }
                
                System.out.print("End date (yyyy-MM-dd): ");
                String ed = scanner.nextLine();
                if (!ed.isEmpty()) {
                    LocalDate endDate = LocalDate.parse(ed);
                    filteredEvents = filteredEvents.stream()
                        .filter(e -> !e.getStartDateTime().toLocalDate().isAfter(endDate))
                        .collect(Collectors.toList());
                }
                break;
                
            default:
                System.out.println("Invalid choice.");
                return;
        }
        
        // Display results
        System.out.println("\nSEARCH RESULTS (" + filteredEvents.size() + " events found)");
        System.out.println("-".repeat(60));
        
        if (filteredEvents.isEmpty()) {
            System.out.println("No events match your criteria.");
        } else {
            filteredEvents.sort(Comparator.comparing(Event::getStartDateTime));
            
            for (Event e : filteredEvents) {
                System.out.printf("%s | %s - %s | %s\n",
                    e.getStartDateTime().format(DATE_FORMATTER),
                    e.getStartDateTime().format(TIME_FORMATTER),
                    e.getEndDateTime().format(TIME_FORMATTER),
                    e.getTitle());
            }
        }
    }

    // ===== 5. EVENT STATISTICS =====
    private static void showEventStatistics() {
        List<Event> events = FileManager.loadEvents();
        
        if (events.isEmpty()) {
            System.out.println("\nNo events to analyze.");
            return;
        }
        
        System.out.println("\nEVENT STATISTICS");
        System.out.println("-".repeat(50));
        
        // 1. Total events
        System.out.println("Total Events: " + events.size());
        
        // 2. Events by day of week
        Map<DayOfWeek, Long> eventsByDay = events.stream()
            .collect(Collectors.groupingBy(
                e -> e.getStartDateTime().getDayOfWeek(),
                Collectors.counting()
            ));
        
        System.out.println("\nEvents by Day of Week:");
        for (DayOfWeek day : DayOfWeek.values()) {
            long count = eventsByDay.getOrDefault(day, 0L);
            System.out.printf("  %-10s: %d events (%.1f%%)\n", 
                day, count, (count * 100.0 / events.size()));
        }
        
        // 3. Busiest day
        DayOfWeek busiestDay = eventsByDay.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        System.out.println("\nBusiest Day: " + busiestDay + 
                          " (" + eventsByDay.getOrDefault(busiestDay, 0L) + " events)");
        
        // 4. Events by month
        Map<Month, Long> eventsByMonth = events.stream()
            .collect(Collectors.groupingBy(
                e -> e.getStartDateTime().getMonth(),
                Collectors.counting()
            ));
        
        System.out.println("\nEvents by Month:");
        for (Month month : Month.values()) {
            long count = eventsByMonth.getOrDefault(month, 0L);
            if (count > 0) {
                System.out.printf("  %-10s: %d events\n", month, count);
            }
        }
        
        // 5. Average event duration
        double avgDuration = events.stream()
            .mapToLong(e -> java.time.Duration.between(e.getStartDateTime(), e.getEndDateTime()).toMinutes())
            .average()
            .orElse(0.0);
        
        System.out.printf("\nAverage Event Duration: %.1f minutes\n", avgDuration);
        
        // 6. Earliest and latest events
        Event earliest = events.stream()
            .min(Comparator.comparing(Event::getStartDateTime))
            .orElse(null);
        
        Event latest = events.stream()
            .max(Comparator.comparing(Event::getStartDateTime))
            .orElse(null);
        
        if (earliest != null) {
            System.out.println("\nEarliest Event: " + earliest.getTitle() + 
                             " on " + earliest.getStartDateTime().format(DATETIME_FORMATTER));
        }
        
        if (latest != null) {
            System.out.println("Latest Event: " + latest.getTitle() + 
                             " on " + latest.getStartDateTime().format(DATETIME_FORMATTER));
        }
        
        // 7. Recurring events count
        List<Recurrence> recs = FileManager.loadRecurrences();
        System.out.println("\nRecurring Events: " + recs.size() + " unique patterns");
    }

    // ===== 6. BACKUP/RESTORE =====
    private static void backupRestoreMenu() {
        System.out.println("\nBACKUP & RESTORE");
        System.out.println("-".repeat(30));
        System.out.println("1. Create Backup");
        System.out.println("2. Restore from Backup");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine();
        System.out.print("Enter folder path: ");
        String path = scanner.nextLine();
        
        if (choice.equals("1")) {
            FileManager.backup(path);
        } else if (choice.equals("2")) {
            System.out.print("WARNING: This will overwrite current data. Continue? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                FileManager.restore(path);
            }
        } else {
            System.out.println("Invalid choice.");
        }
    }

    // ===== 7. CONFLICT DETECTION =====
    private static void checkEventConflicts() {
        List<Event> events = FileManager.loadEvents();
        System.out.println("\nEVENT CONFLICT CHECK");
        System.out.println("-".repeat(50));
        
        boolean foundConflicts = false;
        
        for (int i = 0; i < events.size(); i++) {
            for (int j = i + 1; j < events.size(); j++) {
                Event e1 = events.get(i);
                Event e2 = events.get(j);
                
                if (e1.overlapsWith(e2)) {
                    System.out.println("\nCONFLICT DETECTED:");
                    System.out.println("   Event 1: " + e1.getTitle() + 
                                     " (" + e1.getStartDateTime().format(DATETIME_FORMATTER) + 
                                     " - " + e1.getEndDateTime().format(DATETIME_FORMATTER) + ")");
                    System.out.println("   Event 2: " + e2.getTitle() + 
                                     " (" + e2.getStartDateTime().format(DATETIME_FORMATTER) + 
                                     " - " + e2.getEndDateTime().format(DATETIME_FORMATTER) + ")");
                    foundConflicts = true;
                }
            }
        }
        
        if (!foundConflicts) {
            System.out.println("OK: No scheduling conflicts found!");
        }
    }

    // ===== 8. ADDITIONAL FEATURES =====
    private static void additionalFeaturesMenu() {
        System.out.println("\nADDITIONAL FEATURES");
        System.out.println("-".repeat(30));
        System.out.println("1. Set Event Reminders");
        System.out.println("2. Export Events to Text File");
        System.out.println("3. View Upcoming Events");
        System.out.println("4. Quick Add (Default Times)");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine();
        
        switch (choice) {
            case "1": setReminders(); break;
            case "2": exportToFile(); break;
            case "3": viewUpcomingEvents(); break;
            case "4": quickAddEvent(); break;
            default: System.out.println("Invalid choice.");
        }
    }

    private static void setReminders() {
        System.out.println("\nSET REMINDERS");
        System.out.println("-".repeat(30));
        
        System.out.print("Enter Event ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            
            List<Event> events = FileManager.loadEvents();
            Event target = null;
            
            for (Event e : events) {
                if (e.getEventId() == id) {
                    target = e;
                    break;
                }
            }
            
            if (target == null) {
                System.out.println("ERROR: Event not found.");
                return;
            }
            
            System.out.println("\nEvent: " + target.getTitle());
            System.out.println("Time: " + target.getStartDateTime().format(DATETIME_FORMATTER));
            
            System.out.println("\nReminder Options:");
            System.out.println("1. 15 minutes before");
            System.out.println("2. 30 minutes before");
            System.out.println("3. 1 hour before");
            System.out.println("4. 1 day before");
            System.out.println("5. Custom time");
            System.out.print("Choice: ");
            
            String option = scanner.nextLine();
            Duration reminderTime = Duration.ZERO;
            
            switch (option) {
                case "1": reminderTime = Duration.ofMinutes(15); break;
                case "2": reminderTime = Duration.ofMinutes(30); break;
                case "3": reminderTime = Duration.ofHours(1); break;
                case "4": reminderTime = Duration.ofDays(1); break;
                case "5": 
                    System.out.print("Enter minutes before event: ");
                    int minutes = Integer.parseInt(scanner.nextLine());
                    reminderTime = Duration.ofMinutes(minutes);
                    break;
                default:
                    System.out.println("Invalid option.");
                    return;
            }
            
            LocalDateTime reminderDateTime = target.getStartDateTime().minus(reminderTime);
            System.out.println("\nSUCCESS: Reminder set for: " + reminderDateTime.format(DATETIME_FORMATTER));
            
            // Save reminder to a simple text file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("reminders.txt", true))) {
                writer.write(String.format("Event: %s | Reminder: %s | Event Time: %s\n",
                    target.getTitle(),
                    reminderDateTime.format(DATETIME_FORMATTER),
                    target.getStartDateTime().format(DATETIME_FORMATTER)));
                writer.newLine();
            } catch (IOException e) {
                System.out.println("Note: Could not save reminder to file.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid ID format.");
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void exportToFile() {
        System.out.println("\nEXPORT EVENTS");
        System.out.println("-".repeat(30));
        
        System.out.print("Enter filename (e.g., events_export.txt): ");
        String filename = scanner.nextLine();
        
        List<Event> events = FileManager.loadEvents();
        events.sort(Comparator.comparing(Event::getStartDateTime));
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("CALENDAR EVENT EXPORT");
            writer.newLine();
            writer.write("Generated: " + LocalDateTime.now().format(DATETIME_FORMATTER));
            writer.newLine();
            writer.write("Total Events: " + events.size());
            writer.newLine();
            writer.newLine();
            
            for (Event e : events) {
                writer.write("=".repeat(50));
                writer.newLine();
                writer.write("Title: " + e.getTitle());
                writer.newLine();
                writer.write("Date: " + e.getStartDateTime().format(DATE_FORMATTER));
                writer.newLine();
                writer.write("Time: " + e.getStartDateTime().format(TIME_FORMATTER) + 
                           " - " + e.getEndDateTime().format(TIME_FORMATTER));
                writer.newLine();
                writer.write("Description: " + e.getDescription());
                writer.newLine();
                writer.write("Event ID: " + e.getEventId());
                writer.newLine();
                writer.newLine();
            }
            
            System.out.println("SUCCESS: Events exported to " + filename);
            
        } catch (IOException e) {
            System.out.println("ERROR exporting: " + e.getMessage());
        }
    }

    private static void viewUpcomingEvents() {
        System.out.println("\nUPCOMING EVENTS");
        System.out.println("-".repeat(50));
        
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = FileManager.loadEvents();
        
        List<Event> upcoming = events.stream()
            .filter(e -> e.getStartDateTime().isAfter(now))
            .sorted(Comparator.comparing(Event::getStartDateTime))
            .limit(10)
            .collect(Collectors.toList());
        
        if (upcoming.isEmpty()) {
            System.out.println("No upcoming events.");
        } else {
            System.out.println("Next " + upcoming.size() + " upcoming events:");
            System.out.println();
            
            for (Event e : upcoming) {
                Duration timeUntil = Duration.between(now, e.getStartDateTime());
                long days = timeUntil.toDays();
                long hours = timeUntil.toHoursPart();
                long minutes = timeUntil.toMinutesPart();
                
                String timeUntilStr = "";
                if (days > 0) timeUntilStr += days + " days ";
                if (hours > 0) timeUntilStr += hours + " hours ";
                if (minutes > 0) timeUntilStr += minutes + " minutes ";
                
                System.out.printf("- %s\n", e.getTitle());
                System.out.printf("  Time: %s (in %s)\n", 
                    e.getStartDateTime().format(DATETIME_FORMATTER),
                    timeUntilStr);
                System.out.printf("  Description: %s\n", e.getDescription());
                System.out.println();
            }
        }
    }

    private static void quickAddEvent() {
        System.out.println("\nQUICK ADD EVENT");
        System.out.println("-".repeat(30));
        
        try {
            System.out.print("Title: ");
            String title = scanner.nextLine();
            
            System.out.print("Date (yyyy-MM-dd) [Today]: ");
            String dateStr = scanner.nextLine();
            LocalDate date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);
            
            // Default time slots
            System.out.println("\nDefault Time Slots:");
            System.out.println("1. Morning (09:00 - 10:00)");
            System.out.println("2. Afternoon (14:00 - 15:00)");
            System.out.println("3. Evening (19:00 - 20:00)");
            System.out.print("Choose slot (1-3): ");
            
            String slot = scanner.nextLine();
            LocalTime startTime, endTime;
            
            switch (slot) {
                case "1": startTime = LocalTime.of(9, 0); endTime = LocalTime.of(10, 0); break;
                case "2": startTime = LocalTime.of(14, 0); endTime = LocalTime.of(15, 0); break;
                case "3": startTime = LocalTime.of(19, 0); endTime = LocalTime.of(20, 0); break;
                default:
                    System.out.println("Invalid choice. Using Morning slot.");
                    startTime = LocalTime.of(9, 0); endTime = LocalTime.of(10, 0);
            }
            
            int newId = FileManager.getNextId();
            Event newEvent = new Event(newId, title, "Quick added event",
                LocalDateTime.of(date, startTime),
                LocalDateTime.of(date, endTime));
            
            FileManager.saveEvent(newEvent);
            System.out.println("SUCCESS: Event added successfully! ID: " + newId);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // ===== STARTUP REMINDERS =====
    private static void checkRemindersOnStartup() {
        System.out.println("\nWELCOME TO CALENDAR APP");
        System.out.println("-".repeat(50));
        
        LocalDateTime now = LocalDateTime.now();
        List<Event> events = FileManager.loadEvents();
        
        // Check for events happening now
        List<Event> happeningNow = events.stream()
            .filter(e -> !e.getStartDateTime().isAfter(now) && e.getEndDateTime().isAfter(now))
            .collect(Collectors.toList());
        
        if (!happeningNow.isEmpty()) {
            System.out.println("EVENTS HAPPENING NOW:");
            for (Event e : happeningNow) {
                System.out.printf("- %s (until %s)\n", 
                    e.getTitle(), e.getEndDateTime().format(TIME_FORMATTER));
            }
            System.out.println();
        }
        
        // Check for upcoming events today
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        List<Event> upcomingToday = events.stream()
            .filter(e -> e.getStartDateTime().isAfter(now) && e.getStartDateTime().isBefore(endOfDay))
            .sorted(Comparator.comparing(Event::getStartDateTime))
            .collect(Collectors.toList());
        
        if (!upcomingToday.isEmpty()) {
            System.out.println("UPCOMING TODAY:");
            for (Event e : upcomingToday) {
                Duration timeUntil = Duration.between(now, e.getStartDateTime());
                System.out.printf("- %s in %d hours, %d minutes\n",
                    e.getTitle(), timeUntil.toHours(), timeUntil.toMinutesPart());
            }
            System.out.println();
        }
        
        // Count events for tomorrow
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        long tomorrowEvents = events.stream()
            .filter(e -> e.getStartDateTime().toLocalDate().equals(tomorrow))
            .count();
        
        if (tomorrowEvents > 0) {
            System.out.printf("You have %d event(s) scheduled for tomorrow.\n", tomorrowEvents);
        }
    }
}
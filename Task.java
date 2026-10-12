public class Task {
    private final String name;
    private final String description;
    /** Validates and trims the name and optional description. */
    public Task(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Task name can't be blank.");
        }
        String cleanName = name.trim();
        String cleanDescription = description == null ? "" : description.trim();
        if (cleanName.length() > 60) {
            throw new IllegalArgumentException("Keep names to 60 characters.");
        }
        if (cleanDescription.length() > 200) {
            throw new IllegalArgumentException("Keep descriptions to 200 characters.");
        }
        this.name = cleanName; this.description = cleanDescription;
    }
    /** Returns the validated name. */
    public String getName() { return name; }
    /** Returns the description, possibly empty. */
    public String getDescription() { return description; }
    /** Returns the task name. */
    @Override
    public String toString() { return name; }
}

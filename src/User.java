/**
 * Field-based POJO representing a user in the system.
 *
 * Session M3 Part A: use Copilot ghost text to convert this class
 * into a compact record (see UserDTO.java for where the record goes).
 */
public class User {
    private long id;
    private String name;
    private String email;
    private boolean active;

    public User() {}

    public User(long id, String name, String email, boolean active) {
        this.id     = id;
        this.name   = name;
        this.email  = email;
        this.active = active;
    }

    public long   getId()     { return id; }
    public String getName()   { return name; }
    public String getEmail()  { return email; }
    public boolean isActive() { return active; }

    public void setId(long id)          { this.id = id; }
    public void setName(String name)    { this.name = name; }
    public void setEmail(String email)  { this.email = email; }
    public void setActive(boolean a)    { this.active = a; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name=" + name + ", email=" + email
             + ", active=" + active + "}";
    }
}

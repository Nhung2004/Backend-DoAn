package hospital.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalTime;

/**
 * A TimeSlot.
 */
@Entity
@Table(
    name = "time_slot",
    uniqueConstraints = @UniqueConstraint(name = "ux_time_slot_schedule_slot_time", columnNames = { "schedule_id", "slot_time" })
)
public class TimeSlot implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    @Column(name = "is_booked")
    private Boolean booked = false;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "doctor", "timeSlots" }, allowSetters = true)
    private Schedule schedule;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalTime getSlotTime() {
        return slotTime;
    }

    public void setSlotTime(LocalTime slotTime) {
        this.slotTime = slotTime;
    }

    public Boolean getBooked() {
        return booked;
    }

    public void setBooked(Boolean booked) {
        this.booked = booked;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeSlot)) {
            return false;
        }
        return id != null && id.equals(((TimeSlot) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "TimeSlot{" + "id=" + getId() + ", slotTime='" + getSlotTime() + "'" + ", booked='" + getBooked() + "'" + "}";
    }
}

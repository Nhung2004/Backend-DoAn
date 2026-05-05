package hospital.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link hospital.domain.Specialty} entity.
 */
public class SpecialtyDTO implements Serializable {

    private Long id;

    private String name;

    private String vietnamName;

    private String icon;

    private Integer doctorCount;

    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVietnamName() {
        return vietnamName;
    }

    public void setVietnamName(String vietnamName) {
        this.vietnamName = vietnamName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getDoctorCount() {
        return doctorCount;
    }

    public void setDoctorCount(Integer doctorCount) {
        this.doctorCount = doctorCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpecialtyDTO)) {
            return false;
        }

        SpecialtyDTO specialtyDTO = (SpecialtyDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, specialtyDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SpecialtyDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", vietnamName='" + getVietnamName() + "'" +
            ", icon='" + getIcon() + "'" +
            ", doctorCount=" + getDoctorCount() +
            ", description='" + getDescription() + "'" +
            "}";
    }
}

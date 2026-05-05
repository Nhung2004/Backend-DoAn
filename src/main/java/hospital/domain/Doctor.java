package hospital.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A Doctor.
 */
@Entity
@Table(name = "doctor")
public class Doctor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "bio")
    private String bio;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "experience")
    private Integer experience;

    @Column(name = "license")
    private String license;

    @Column(name = "price")
    private Long price;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "doctors" }, allowSetters = true)
    private Specialty specialty;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "doctors" }, allowSetters = true)
    private Hospital hospital;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public Doctor fullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public Doctor email(String email) {
        this.email = email;
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getBio() {
        return bio;
    }

    public Doctor bio(String bio) {
        this.bio = bio;
        return this;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatar() {
        return avatar;
    }

    public Doctor avatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getExperience() {
        return experience;
    }

    public Doctor experience(Integer experience) {
        this.experience = experience;
        return this;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public String getLicense() {
        return license;
    }

    public Doctor license(String license) {
        this.license = license;
        return this;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public Long getPrice() {
        return price;
    }

    public Doctor price(Long price) {
        this.price = price;
        return this;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Double getRating() {
        return rating;
    }

    public Doctor rating(Double rating) {
        this.rating = rating;
        return this;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public Doctor reviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
        return this;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }

    public Doctor specialty(Specialty specialty) {
        this.setSpecialty(specialty);
        return this;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public Doctor hospital(Hospital hospital) {
        this.setHospital(hospital);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Doctor)) {
            return false;
        }
        return id != null && id.equals(((Doctor) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "Doctor{" +
            "id=" +
            getId() +
            ", fullName='" +
            getFullName() +
            "'" +
            ", email='" +
            getEmail() +
            "'" +
            ", phoneNumber='" +
            getPhoneNumber() +
            "'" +
            ", bio='" +
            getBio() +
            "'" +
            ", avatar='" +
            getAvatar() +
            "'" +
            ", experience=" +
            getExperience() +
            ", license='" +
            getLicense() +
            "'" +
            ", price=" +
            getPrice() +
            ", rating=" +
            getRating() +
            ", reviewCount=" +
            getReviewCount() +
            "}"
        );
    }
}

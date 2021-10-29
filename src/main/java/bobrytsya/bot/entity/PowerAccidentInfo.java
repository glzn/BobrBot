package bobrytsya.bot.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
public class PowerAccidentInfo {

    @Id
    @GeneratedValue
    private Long id;
    private LocalDate accidentDate;
    private String accidentTime;
    private String resumptionTime;
    @Column(columnDefinition = "text")
    private String street;

    public Long getId() {
        return id;
    }

    public LocalDate getAccidentDate() {
        return accidentDate;
    }

    public void setAccidentDate(LocalDate accidentDate) {
        this.accidentDate = accidentDate;
    }

    public String getAccidentTime() {
        return accidentTime;
    }

    public void setAccidentTime(String accidentTime) {
        this.accidentTime = accidentTime;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getResumptionTime() {
        return resumptionTime;
    }

    public void setResumptionTime(String resumptionTime) {
        this.resumptionTime = resumptionTime;
    }

    public boolean equals(Object o) {
        if (!(o instanceof PowerAccidentInfo)){
            return false;
        }
        PowerAccidentInfo powerAccidentInfo = (PowerAccidentInfo) o;
        return powerAccidentInfo.getAccidentTime().equals(accidentTime)
                && powerAccidentInfo.getStreet().equals(street)
                && powerAccidentInfo.getAccidentDate().equals(accidentDate);
    }
}

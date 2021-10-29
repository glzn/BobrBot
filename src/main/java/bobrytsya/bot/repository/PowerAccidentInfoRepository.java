package bobrytsya.bot.repository;

import bobrytsya.bot.entity.PowerAccidentInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PowerAccidentInfoRepository extends JpaRepository<PowerAccidentInfo, Long> {
    List<PowerAccidentInfo> findAllByAccidentDateIs(LocalDate accidentDate);
}

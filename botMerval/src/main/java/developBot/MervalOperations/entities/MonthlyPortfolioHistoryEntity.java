package developBot.MervalOperations.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "MonthlyPortfolioHistories")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyPortfolioHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column
    private LocalDateTime date;
    @Column
    private Double portfolioAmount;
    @Column
    private Double yield;
}

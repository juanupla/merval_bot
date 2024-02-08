package developBot.MervalOperations.entities.EMAs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "emas")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EMAsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column
    private BigDecimal ema3;
    @Column
    private BigDecimal ema9;
    @Column
    private BigDecimal ema21;
    @Column
    private BigDecimal ema50;
}

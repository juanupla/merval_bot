package developBot.MervalOperations.entities.EMAs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "emasByAsset")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmaByAssetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column
    private String simbol;
    @Column
    private LocalDateTime date;
    @ManyToOne
    @JoinColumn(name = "id_emas")
    private EMAsEntity emas;

}

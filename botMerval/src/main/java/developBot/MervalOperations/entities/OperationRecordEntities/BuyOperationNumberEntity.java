package developBot.MervalOperations.entities.OperationRecordEntities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "buyOperationNumbers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuyOperationNumberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column
    private Long number;

}

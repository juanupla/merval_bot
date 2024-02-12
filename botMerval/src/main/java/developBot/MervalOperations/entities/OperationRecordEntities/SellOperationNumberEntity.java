package developBot.MervalOperations.entities.OperationRecordEntities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "sellOperationNumbers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellOperationNumberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column
    private Long number;
    @Column
    private Long amount;
    @ManyToOne
    @JoinColumn(name = "operation_record_id")
    private OperationRecordEntity operationRecordEntity;
}

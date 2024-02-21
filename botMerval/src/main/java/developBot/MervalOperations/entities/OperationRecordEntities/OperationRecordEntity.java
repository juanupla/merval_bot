package developBot.MervalOperations.entities.OperationRecordEntities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "operationsRecord")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "id_buyOperationNumber")
    private BuyOperationNumberEntity buyOperationNumber;
    @OneToMany(mappedBy = "operationRecordId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SellOperationNumberEntity> sellOperationNumbers;
    @Column
    private String simbol;
    @Column
    private LocalDateTime dateOfPurchease;
    @Column
    private Double purcheasePrice;
    @Column
    private Long purcheaseAmount;
    @Column
    private Boolean status; //open-closed
    @Column
    private LocalDateTime saleDate;
    @Column
    private Double averageSellingPrice;
    @Column
    private Long salesAmount;
    @Column
    private Double yield;
    @Column
    private Double annualizedYield;
    @Column
    private String result;


}

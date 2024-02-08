package developBot.MervalOperations.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "operationsLog")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column
    private String simbol;
    @Column
    private LocalDateTime dateOfPurchease;
    @Column
    private Double purcheasePrice;
    @Column
    private Long purcheaseAmount;
    @Column
    private String status; //abierta/cerrada
    @Column
    private LocalDateTime saleDate;
    @Column
    private Double salePrice;
    @Column
    private Long salesAmount;
    @Column
    private Double yield;
    @Column
    private Double annualizedYield;
    @Column
    private String result;


}

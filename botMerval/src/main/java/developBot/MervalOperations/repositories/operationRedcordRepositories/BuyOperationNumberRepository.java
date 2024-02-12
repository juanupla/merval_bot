package developBot.MervalOperations.repositories.operationRedcordRepositories;

import developBot.MervalOperations.entities.OperationRecordEntities.BuyOperationNumberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuyOperationNumberRepository extends JpaRepository<BuyOperationNumberEntity, UUID> {
    Optional<BuyOperationNumberEntity> findByNumber(Long numberOperation);
}

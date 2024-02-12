package developBot.MervalOperations.repositories.operationRedcordRepositories;

import developBot.MervalOperations.entities.OperationRecordEntities.SellOperationNumberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SellOperationNumberRepository extends JpaRepository<SellOperationNumberEntity, UUID> {
}
package developBot.MervalOperations.repositories;

import developBot.MervalOperations.entities.OperationRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OperationRecordRepository extends JpaRepository<OperationRecordEntity, UUID> {

}

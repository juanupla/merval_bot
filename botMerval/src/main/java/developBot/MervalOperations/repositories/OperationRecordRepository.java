package developBot.MervalOperations.repositories;

import developBot.MervalOperations.entities.OperationRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface OperationRecordRepository extends JpaRepository<OperationRecordEntity, UUID> {

}

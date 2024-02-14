package developBot.MervalOperations.repositories.operationRedcordRepositories;

import developBot.MervalOperations.entities.OperationRecordEntities.OperationRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface OperationRecordRepository extends JpaRepository<OperationRecordEntity, UUID> {

    Optional<OperationRecordEntity> findBySimbolAndStatus(String simbol,boolean status);
}

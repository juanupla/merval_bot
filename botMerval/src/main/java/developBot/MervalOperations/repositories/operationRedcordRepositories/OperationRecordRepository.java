package developBot.MervalOperations.repositories.operationRedcordRepositories;

import developBot.MervalOperations.entities.OperationRecordEntities.OperationRecordEntity;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface OperationRecordRepository extends JpaRepository<OperationRecordEntity, UUID> {

    Optional<OperationRecordEntity> findBySimbolAndStatus(String simbol,boolean status);
    @Query("SELECT s FROM OperationRecordEntity s WHERE s.result IS NULL")
    Optional<List<OperationRecordEntity>> findNullResult();
}

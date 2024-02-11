package developBot.MervalOperations.repositories;

import developBot.MervalOperations.entities.MonthlyPortfolioHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface MonthlyPortfolioHistoryRepository extends JpaRepository<MonthlyPortfolioHistoryEntity, UUID> {
}

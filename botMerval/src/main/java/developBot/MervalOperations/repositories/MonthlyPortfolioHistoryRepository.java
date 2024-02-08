package developBot.MervalOperations.repositories;

import developBot.MervalOperations.entities.MonthlyPortfolioHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MonthlyPortfolioHistoryRepository extends JpaRepository<MonthlyPortfolioHistoryEntity, UUID> {
}

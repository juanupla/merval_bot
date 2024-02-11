package developBot.MervalOperations.repositories;

import developBot.MervalOperations.entities.EMAs.EmaByAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface EmaByAssetRepository extends JpaRepository<EmaByAssetEntity, UUID> {
}

package developBot.MervalOperations.repositories;

import developBot.MervalOperations.entities.EMAs.EmaByAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmaByAssetRepository extends JpaRepository<EmaByAssetEntity, UUID> {
}

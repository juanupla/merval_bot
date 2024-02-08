package developBot.MervalOperations.repositories;

import developBot.MervalOperations.entities.EMAs.EMAsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EMAsRepository extends JpaRepository<EMAsEntity, UUID> {
}

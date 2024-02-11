package developBot.MervalOperations.repositories;

import developBot.MervalOperations.entities.EMAs.EMAsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface EMAsRepository extends JpaRepository<EMAsEntity, UUID> {
}

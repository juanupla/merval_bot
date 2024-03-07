package developBot.MervalOperations.services.mervalBotDataService;

import developBot.MervalOperations.models.OperationRecord.BuyOperationNumber;
import org.springframework.stereotype.Service;

@Service
public interface BuyOperationNumberService {

    Boolean exists(Long numberOperation);
    boolean save(BuyOperationNumber buyOperationNumber);
}

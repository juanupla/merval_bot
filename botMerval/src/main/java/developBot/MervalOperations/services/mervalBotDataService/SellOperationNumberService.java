package developBot.MervalOperations.services.mervalBotDataService;

import developBot.MervalOperations.models.OperationRecord.SellOperationNumber;
import org.springframework.stereotype.Service;

@Service
public interface SellOperationNumberService {
    Boolean exist(Long number,Boolean status);
    Boolean save (SellOperationNumber sellOperationNumber);
}

package developBot.MervalOperations.services.mervalBotDataService;

import developBot.MervalOperations.models.dto.OperationRecordDto.BuyOperationNumberDTO;
import org.springframework.stereotype.Service;

@Service
public interface BuyOperationNumberService {

    Boolean exists(Long numberOperation);
    boolean save(BuyOperationNumberDTO buyOperationNumberDTO);
}

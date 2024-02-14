package developBot.MervalOperations.services.mervalBotDataService;

import developBot.MervalOperations.models.dto.OperationRecordDto.SellOperationNumberDTO;
import org.springframework.stereotype.Service;

@Service
public interface SellOperationNumberService {
    Boolean exist(Long number,Boolean status);
    Boolean save (SellOperationNumberDTO sellOperationNumberDTO);
}

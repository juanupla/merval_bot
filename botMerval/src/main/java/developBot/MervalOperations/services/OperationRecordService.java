package developBot.MervalOperations.services;

import developBot.MervalOperations.models.dto.OperationRecordDTO;
import org.springframework.stereotype.Service;

@Service
public interface OperationRecordService {
    boolean updateOperationsDataBase(OperationRecordDTO operationRecordDTO);
}

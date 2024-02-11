package developBot.MervalOperations.services.impl;

import developBot.MervalOperations.models.dto.OperationRecordDTO;
import developBot.MervalOperations.services.OperationRecordService;
import org.springframework.stereotype.Service;

@Service
public class OperationRecordServiceImpl implements OperationRecordService {
    @Override
    public boolean updateOperationsDataBase(OperationRecordDTO operationRecordDTO) {



        return true;
    }
}

package developBot.MervalOperations.services.mervalBotDataService;

import org.springframework.stereotype.Service;

@Service
public interface OperationRecordService {
    boolean updateOperationsDataBase(String token);
}

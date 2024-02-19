package developBot.MervalOperations.services.mervalBotDataService;

import developBot.MervalOperations.models.clientModel.miCuenta.operaciones.Operacion;
import org.springframework.stereotype.Service;

@Service
public interface OperationRecordService {
    boolean updateOperationsDataBase(String token);
    void closedOperationRecordProcessor();
}

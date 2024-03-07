package developBot.MervalOperations.services.mervalBotDataService.impl;

import developBot.MervalOperations.entities.OperationRecordEntities.BuyOperationNumberEntity;
import developBot.MervalOperations.models.OperationRecord.BuyOperationNumber;
import developBot.MervalOperations.repositories.operationRedcordRepositories.BuyOperationNumberRepository;
import developBot.MervalOperations.services.mervalBotDataService.BuyOperationNumberService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BuyOperationNumberServiceImpl implements BuyOperationNumberService {

    @Autowired
    private BuyOperationNumberRepository buyOperationNumberRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Override
    public Boolean exists(Long numberOperation) {

        Optional<BuyOperationNumberEntity> buyOperationNumberEntity = buyOperationNumberRepository.findByNumber(numberOperation);
        if(buyOperationNumberEntity.isPresent()){
            BuyOperationNumber buyOperationNumber =modelMapper.map(buyOperationNumberEntity, BuyOperationNumber.class);
            if (buyOperationNumber.getNumber().equals(numberOperation)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean save(BuyOperationNumber buyOperationNumber) {
        BuyOperationNumberEntity buyOperationNumberEntity = buyOperationNumberRepository.save(modelMapper.map(buyOperationNumber,BuyOperationNumberEntity.class));
        if(buyOperationNumberEntity != null){
            return true;
        }
        throw new RuntimeException("error al guardar operacion de compra(BuyOperationNumberServiceImpl)");
    }
}

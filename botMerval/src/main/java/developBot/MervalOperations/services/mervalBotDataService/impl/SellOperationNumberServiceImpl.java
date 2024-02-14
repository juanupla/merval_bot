package developBot.MervalOperations.services.mervalBotDataService.impl;

import developBot.MervalOperations.entities.OperationRecordEntities.SellOperationNumberEntity;
import developBot.MervalOperations.models.dto.OperationRecordDto.SellOperationNumberDTO;
import developBot.MervalOperations.repositories.operationRedcordRepositories.SellOperationNumberRepository;
import developBot.MervalOperations.services.mervalBotDataService.SellOperationNumberService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SellOperationNumberServiceImpl implements SellOperationNumberService {
    @Autowired
    private SellOperationNumberRepository sellOperationNumberRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Override
    public Boolean exist(Long number, Boolean status) {

        Optional<SellOperationNumberEntity> sellOperationNumberEntity = sellOperationNumberRepository.findByNumberAndStatus(number,status);
        if(sellOperationNumberEntity.isPresent()){
            return true;
        }
        return false;
    }

    @Override
    public Boolean save(SellOperationNumberDTO sellOperationNumberDTO) {

        SellOperationNumberEntity sellOperationNumberEntity = sellOperationNumberRepository.save(modelMapper.map(sellOperationNumberDTO,SellOperationNumberEntity.class));
        if (sellOperationNumberEntity != null){
            return true;
        }
        throw new RuntimeException("error al guardar operacion de venta(SellOperationNumberServiceImpl)");
    }
}

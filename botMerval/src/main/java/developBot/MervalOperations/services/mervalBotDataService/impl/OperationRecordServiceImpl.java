package developBot.MervalOperations.services.mervalBotDataService.impl;

import developBot.MervalOperations.entities.OperationRecordEntities.OperationRecordEntity;
import developBot.MervalOperations.models.clientModel.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.dto.OperationRecordDto.BuyOperationNumberDTO;
import developBot.MervalOperations.models.dto.OperationRecordDto.OperationRecordDTO;
import developBot.MervalOperations.repositories.operationRedcordRepositories.BuyOperationNumberRepository;
import developBot.MervalOperations.repositories.operationRedcordRepositories.OperationRecordRepository;
import developBot.MervalOperations.services.iolApiService.CallsApiIOLBusinessService;
import developBot.MervalOperations.services.mervalBotDataService.BuyOperationNumberService;
import developBot.MervalOperations.services.mervalBotDataService.OperationRecordService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationRecordServiceImpl implements OperationRecordService {
    @Autowired
    private CallsApiIOLBusinessService callsApiIOLBusinessService;
    @Autowired
    private BuyOperationNumberService buyOperationNumberService;
    @Autowired
    private OperationRecordRepository operationRecordRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Override
    public boolean updateOperationsDataBase(String token) {
        Operacion[] operations = callsApiIOLBusinessService.getEndOfTheDayTrades(token);

        for (Operacion operacion:operations) {
            //si es venta o compra y si la operacion esta terminada.-Si no es terminada(puede ser cancelada) pero realizo operaciones entra.
            processSingleOperation(operacion);
        }

        return true;
    }

    @Transactional
    public void processSingleOperation(Operacion operacion) {

        if (operacion.getTipo().equals("Compra") && (operacion.getEstado().equals("terminada") && operacion.getCantidadOperada() != null)) {
            if (!buyOperationNumberService.exists(operacion.getNumero().longValue())) {
                BuyOperationNumberDTO buyOperationNumberDTO = new BuyOperationNumberDTO();
                buyOperationNumberDTO.setNumber(operacion.getNumero().longValue());

                if (buyOperationNumberService.save(buyOperationNumberDTO)) {
                    System.out.println("Operación de compra registrada");

                    OperationRecordDTO operationRecordDTO = new OperationRecordDTO();
                    operationRecordDTO.setSimbol(operacion.getSimbolo());
                    operationRecordDTO.setDateOfPurchase(operacion.getFechaOperada());
                    operationRecordDTO.setPurchasePrice(operacion.getPrecioOperado());
                    operationRecordDTO.setPurchaseAmount(operacion.getCantidad());
                    operationRecordDTO.setStatus(true); // 0 = operación activa

                    OperationRecordEntity operationRecordEntitySave = operationRecordRepository.save(modelMapper.map(operationRecordDTO, OperationRecordEntity.class));
                    System.out.println("Operación de registro guardada correctamente");
                } else {
                    System.out.println("Operación de compra NO registrada");
                    throw new RuntimeException("Error al registrar operación de compra");
                }
            }
        } else if (operacion.getTipo().equals("Venta") && (operacion.getEstado().equals("terminada") && operacion.getCantidadOperada() != null)) {
            //si es venta tenemos que verificar que no haya sido cargada
            //si no esta la cargamos




            //verificamos contra OperacionRecord que exista el activo y cantidad que sea cancelada con esta venta

            
        } else {

        }
    }
}


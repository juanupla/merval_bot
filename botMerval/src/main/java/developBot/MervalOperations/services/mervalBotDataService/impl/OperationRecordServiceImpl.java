package developBot.MervalOperations.services.mervalBotDataService.impl;

import developBot.MervalOperations.entities.OperationRecordEntities.OperationRecordEntity;
import developBot.MervalOperations.models.clientModel.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.dto.OperationRecordDto.BuyOperationNumberDTO;
import developBot.MervalOperations.models.dto.OperationRecordDto.OperationRecordDTO;
import developBot.MervalOperations.models.dto.OperationRecordDto.SellOperationNumberDTO;
import developBot.MervalOperations.repositories.operationRedcordRepositories.BuyOperationNumberRepository;
import developBot.MervalOperations.repositories.operationRedcordRepositories.OperationRecordRepository;
import developBot.MervalOperations.services.iolApiService.CallsApiIOLBusinessService;
import developBot.MervalOperations.services.mervalBotDataService.BuyOperationNumberService;
import developBot.MervalOperations.services.mervalBotDataService.OperationRecordService;
import developBot.MervalOperations.services.mervalBotDataService.SellOperationNumberService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OperationRecordServiceImpl implements OperationRecordService {
    @Autowired
    private CallsApiIOLBusinessService callsApiIOLBusinessService;
    @Autowired
    private BuyOperationNumberService buyOperationNumberService;
    @Autowired
    private OperationRecordRepository operationRecordRepository;
    @Autowired
    private SellOperationNumberService sellOperationNumberService;

    @Autowired
    private ModelMapper modelMapper;
    @Override
    public boolean updateOperationsDataBase(String token) {
        Operacion[] operations = callsApiIOLBusinessService.getEndOfTheDayTrades(token);

        for (Operacion operacion:operations) {
            //si es venta o compra y si la operacion esta terminada.-Si no es terminada(puede ser cancelada) pero realizo operaciones entra.
            try {
                processSingleOperation(operacion);
            }catch (Error e){
                continue;
            }
        }

        return true;
    }

    @Transactional
    public void processSingleOperation(Operacion operacion) {

        if (operacion.getTipo().equals("Compra") && (operacion.getEstado().equals("terminada") || (operacion.getEstado().equals("cancelada") && operacion.getCantidadOperada() != null))) {
            if (!buyOperationNumberService.exists(operacion.getNumero().longValue())) {
                //si no existe guardamos la operacion de compra

                BuyOperationNumberDTO buyOperationNumberDTO = new BuyOperationNumberDTO();
                buyOperationNumberDTO.setNumber(operacion.getNumero().longValue());
                buyOperationNumberDTO.setSimbol(operacion.getSimbolo());
                buyOperationNumberDTO.setAmount(operacion.getCantidadOperada().longValue());

                if (buyOperationNumberService.save(buyOperationNumberDTO)) {

                    //si se guardo la operacion de compra, generamos en operationRecord(contendrá la compra/venta de activos)

                    OperationRecordDTO operationRecordDTO = new OperationRecordDTO();
                    operationRecordDTO.setSimbol(operacion.getSimbolo());
                    operationRecordDTO.setDateOfPurchase(operacion.getFechaOperada());
                    operationRecordDTO.setPurchasePrice(operacion.getPrecioOperado());
                    operationRecordDTO.setPurchaseAmount(operacion.getCantidad());
                    operationRecordDTO.setStatus(true); // 0 = operación activa

                    OperationRecordEntity operationRecordEntitySave = operationRecordRepository.save(modelMapper.map(operationRecordDTO, OperationRecordEntity.class));
                    if(operationRecordEntitySave != null){
                        System.out.println("operationRecord guardada correctamente");
                    }else {
                        throw new RuntimeException("operationRecord NO guardada. Error");
                    }
                }
            }
        } else if (operacion.getTipo().equals("Venta") && (operacion.getEstado().equals("terminada") || (operacion.getEstado().equals("cancelada") && operacion.getCantidadOperada() != null))) {
            //si es venta tenemos que verificar que no haya sido cargada
            //si no esta la cargamos

            //preguntamos si existe esta orden y que el status sea cerrado.Si es abierto todavia hay operaciones disponibles
            if(!sellOperationNumberService.exist(operacion.getNumero().longValue(),false)){
                SellOperationNumberDTO sellOperationNumberDTO = new SellOperationNumberDTO();
                //si la operacion de venta no esta registrada, la guardamos.
                if(!sellOperationNumberService.exist(operacion.getNumero().longValue(),true)){

                    sellOperationNumberDTO.setNumber(operacion.getNumero().longValue());
                    sellOperationNumberDTO.setAmount(operacion.getCantidad());
                    sellOperationNumberDTO.setSimbol(operacion.getSimbolo());
                    if(!sellOperationNumberService.save(sellOperationNumberDTO)){
                        throw new RuntimeException();
                    }
                }


                //si sellOperationNumber se guarda, procedemos a buscar el operationRecord que tenga registrada la
                //compra de este activo en cuestion(venta).
                //debemos considerar que la venta puede estar realizada con 1 o mas SeellOperationNumber
                //debemos buscar el operationRecord(que contendra las compras) donde debemos buscar aquellas
                //con status: open-closed y que coincide con nuestro activo(simbolo), y colocar la cantidad de ventas de este activo
                //en operationRecord. Podra ser el total de lo que se compro o menor(si es menor,
                //sellOperation tendra una cantidad residual)
                Optional<OperationRecordEntity> operationRecordEntityOpt = operationRecordRepository.findBySimbolAndStatus(sellOperationNumberDTO.getSimbol(),true);
                if (operationRecordEntityOpt.isEmpty()){
                    throw new RuntimeException();
                }
                OperationRecordDTO operationRecordDTO=modelMapper.map(operationRecordEntityOpt.get(),OperationRecordDTO.class);
                //cantidad vendida
                Long quantitysold = operationRecordDTO.getSalesAmount();
                //hacer esto en otro metodo
                if (quantitysold == null || quantitysold==0){//si nunca realizo una venta este espacio estara en null o 0-> Se usa el total del purchase
                    //vamos a usar el total de operationRecordDTO.getPurchaseAmount()

                    if(operationRecordDTO.getPurchaseAmount()>=sellOperationNumberDTO.getAmount()){
                        // en este caso tendria que actualizar sellOperationNumber
                        //actualizar el valor residual a 0. Ya que se completa el total de la operacion de venta
                        //si t0do va bien, restamos el total del purchase menos sellOperationNumberDTO.getAmount()
                        //dejamos el operationRecord en estado activo, y le cargamos el dato de saleAmount con la cantidad
                        //que se concreto y actualizamos operationRecord

                        //actualizamos valor residual
                        sellOperationNumberDTO.setResidualQuantity(0L);
                        if (sellOperationNumberService.save(sellOperationNumberDTO)){
                            //si t0do va bien
                            Long result = operationRecordDTO.getPurchaseAmount()-sellOperationNumberDTO.getAmount();
                            operationRecordDTO.setSalesAmount(result);
                            if(operationRecordDTO.getPurchaseAmount().equals(sellOperationNumberDTO.getAmount())){
                                operationRecordDTO.setStatus(false); //si la cantidad que cierro son iguales, operationRecord que completa y cerrada
                            }
                            OperationRecordEntity operationRecordSaved = operationRecordRepository.save(modelMapper.map(operationRecordDTO,OperationRecordEntity.class));
                            if (operationRecordSaved == null){
                                throw new RuntimeException();
                            }
                        }
                        else {
                            throw new RuntimeException();
                        }

                    }else {
                        //en este caso puedo cubrir la el total de ventas de operatonalRecord y va a quedar cerrada
                        //y vamos a tener que actualizar el residualQuantity, previamente.
                        //dicho esto, esta venta quedara con
                        sellOperationNumberDTO.setResidualQuantity(0L);
                        if (sellOperationNumberService.save(sellOperationNumberDTO)){
                            Long sellOperationResidual
                        }


                        }
                    } else if (quantitysold > 0 ){  //si ya se realizo ventas en el operationRecord hay 2 opciones
                                                // deja a la c/compra a cancelar en 2 caminos.. o es mas alto o es mas bajo que mi cantidad de venta
                        Long purchaseStoCancel = operationRecordDTO.getPurchaseAmount() - operationRecordDTO.getSalesAmount();

                        if(purchaseStoCancel > sellOperationNumberDTO.getAmount()){
                            //a la cantidad vendida de operationRecord le sumo el total de sellOperationNumberDTO.getAmount()
                            //previamente debo actualizar el sellOperation respecto a la cantidad resiudal = 0, si eso
                            //va bien, hacemos lo anterior. dejamos la operationRecord activa

                            sellOperationNumberDTO.setResidualQuantity(0L);
                            if(sellOperationNumberService.save(sellOperationNumberDTO)){
                                operationRecordDTO.setSalesAmount(operationRecordDTO.getSalesAmount()+sellOperationNumberDTO.getAmount());
                            }

                        } else if (purchaseStoCancel < sellOperationNumberDTO.getAmount()) {

                        } else{

                        }



                    }
                    else {
                        throw new RuntimeException("operationRecord NO guardada. Error");
                    }
            }


            //verificamos contra OperacionRecord que exista el activo y cantidad que sea cancelada con esta venta


        } else {

        }
    }
}


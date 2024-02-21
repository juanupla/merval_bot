package developBot.MervalOperations.services.mervalBotDataService.impl;

import developBot.MervalOperations.entities.OperationRecordEntities.OperationRecordEntity;
import developBot.MervalOperations.models.clientModel.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.dto.OperationRecordDto.BuyOperationNumberDTO;
import developBot.MervalOperations.models.dto.OperationRecordDto.OperationRecordDTO;
import developBot.MervalOperations.models.dto.OperationRecordDto.SellOperationNumberDTO;
import developBot.MervalOperations.repositories.operationRedcordRepositories.OperationRecordRepository;
import developBot.MervalOperations.services.iolApiService.CallsApiIOLBusinessService;
import developBot.MervalOperations.services.mervalBotDataService.BuyOperationNumberService;
import developBot.MervalOperations.services.mervalBotDataService.OperationRecordService;
import developBot.MervalOperations.services.mervalBotDataService.SellOperationNumberService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
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

        for (Operacion operacion : operations) {
            //si es venta o compra y si la operacion esta terminada.-Si no es terminada(puede ser cancelada) pero realizo operaciones entra.
            try {
                processSingleOperation(operacion);
            } catch (Error e) {
                continue;
            }
        }

        return true;
    }

    @Transactional
    public void processSingleOperation(Operacion operacion) {

        if (operacion.getTipo().equals("Compra") && (operacion.getEstado().equals("terminada") || (operacion.getEstado().equals("cancelada") && operacion.getCantidadOperada() != null))) {

            BuyOperationNumberDTO buyOperationNumberDTO = doesBuyNumberOperationServiceExist(operacion);
            if (buyOperationNumberDTO == null) {
                throw new RuntimeException("este numero de operacion se encuentra registrado");
            }

            OperationRecordDTO operationRecordDTO = saveBuyOperationNumberDTO(buyOperationNumberDTO, operacion);
            if (operationRecordDTO == null) {
                throw new RuntimeException("Error operationalRecord en la compra");
            }


        } else if (operacion.getTipo().equals("Venta") && (operacion.getEstado().equals("terminada") || (operacion.getEstado().equals("cancelada") && operacion.getCantidadOperada() != null))) {
            //si es venta tenemos que verificar que no haya sido cargada
            //si no esta la cargamos
            //preguntamos si existe esta orden y que el status sea cerrado.Si es abierto todavia hay operaciones disponibles

            SellOperationNumberDTO sellOperationNumberDTO = doesSellOperationNumberServiceExist(operacion);
            if (!sellOperationNumberDTO.getStatus()) {
                throw new RuntimeException();//operacion cerrada
            }
            //hasta aca verificamos que el sellOperation no existe o que existe en estado abierto. En otro caso devolvera una exepcion


            //busca operationsRecord en estado abiertas y las procesa
            Optional<OperationRecordEntity> operationRecordEntityOpt = operationRecordRepository.findBySimbolAndStatus(sellOperationNumberDTO.getSimbol(), true);
            if (operationRecordEntityOpt.isEmpty()) {
                //no hay ningun activo (operacion de compra) abierto con este simbolo y/o ese monto
                throw new RuntimeException();
            }

            OperationRecordDTO operationRecordDTO = modelMapper.map(operationRecordEntityOpt.get(), OperationRecordDTO.class);

            //cantidad vendida
            Long quantitysold = operationRecordDTO.getSalesAmount();

            Long purchaseAmount = operationRecordDTO.getPurchaseAmount();

            //este debe encargarse de varias cosas:
            //_luego de concretar la venta (si es que previamente-en este codigo- se encontro un operationalRecord con este simbolo y con compras a cancelar)
            //-debe concretar 2 procesos importantes. Primero actualizar el sellOperation. con cuidado en el residualQuantity y el status
            //si etsa ok, debe actualizar el operationRecord!
            sellEjecution(quantitysold,operationRecordDTO,purchaseAmount, sellOperationNumberDTO);
        }
    }



    //este metodo debe actualizarse
    //ya que al realizar una venta no esta contemplado el status de sellOperation el cual habria que actualizar cada vez que se ejecutan ventas
    @Transactional
    public void sellEjecution(Long quantitysold,OperationRecordDTO operationRecordDTO,Long purchaseAmount,SellOperationNumberDTO sellOperationNumberDTO){
        Long sellAmount;

        //si nunca realizo una venta en este operationalRecord(que contiene una compra) estara en null o 0-> Se usa el total del purchase
        if (quantitysold == null || quantitysold == 0) {

            //primero las cantidades disponibles de compra y las de venta.
            if (sellOperationNumberDTO.getResidualQuantity() == null) {
                sellAmount = sellOperationNumberDTO.getAmount();
            } else {
                sellAmount = sellOperationNumberDTO.getResidualQuantity();
                if (sellAmount == 0) {
                    throw new RuntimeException();//ene este caso no habria nada para vender.es decir, la venta de esta operacion se termino
                }
            }

            boolean fin = isPurchaseGreaterThanOrEqualToTheSale(sellOperationNumberDTO, operationRecordDTO, purchaseAmount, sellAmount);

            if (!fin) {
                if (!isThePurchaseLessThanTheSale(sellAmount, purchaseAmount, sellOperationNumberDTO, operationRecordDTO)) {
                    throw new RuntimeException("la operacion de venta via isPurchaseGreaterThanOrEqualToTheSale no funciono");
                }
            }

        } else if (quantitysold > 0) {
            //si ya se realizo ventas en el operationRecord hay 2 opciones
            //deja a la c/compra a cancelar en 2 caminos.. o es mas alto o es mas bajo que mi cantidad de venta
            long purchaseStoCancel = operationRecordDTO.getPurchaseAmount() - operationRecordDTO.getSalesAmount();

            if (sellOperationNumberDTO.getResidualQuantity() == null) {
                sellAmount = sellOperationNumberDTO.getAmount();
            } else {
                sellAmount = sellOperationNumberDTO.getResidualQuantity();
                if (sellAmount == 0) {
                    throw new RuntimeException();//ene este caso no habria nada para vender.es decir, la venta de esta operacion se termino
                }
            }

            boolean fin = isPurchaseGreaterThanOrEqualToTheSale(sellOperationNumberDTO, operationRecordDTO, purchaseStoCancel, sellAmount);
            if (!fin) {
                if (!isThePurchaseLessThanTheSale(sellAmount, purchaseStoCancel, sellOperationNumberDTO, operationRecordDTO)) {
                    throw new RuntimeException("la operacion de venta via isPurchaseGreaterThanOrEqualToTheSale no funciono");
                }
            }

        }
    }

    public BuyOperationNumberDTO doesBuyNumberOperationServiceExist(Operacion operacion) {
        if (!buyOperationNumberService.exists(operacion.getNumero().longValue())) {
            //si no existe guardamos la operacion de compra

            BuyOperationNumberDTO buyOperationNumberDTO = new BuyOperationNumberDTO();
            buyOperationNumberDTO.setNumber(operacion.getNumero().longValue());
            buyOperationNumberDTO.setSimbol(operacion.getSimbolo());
            buyOperationNumberDTO.setAmount(operacion.getCantidadOperada().longValue());

            return buyOperationNumberDTO;
        } else {
            throw new RuntimeException("este numbero de operacion ya existe");
        }
    }

    public OperationRecordDTO saveBuyOperationNumberDTO(BuyOperationNumberDTO buyOperationNumberDTO, Operacion operacion) {
        //si se guardo la operacion de compra, generamos en operationRecord(contendrá la compra/venta de activos)

        OperationRecordDTO operationRecordDTO = new OperationRecordDTO();
        operationRecordDTO.setSimbol(operacion.getSimbolo());
        operationRecordDTO.setDateOfPurchase(operacion.getFechaOperada());
        operationRecordDTO.setPurchasePrice(operacion.getPrecioOperado());
        operationRecordDTO.setPurchaseAmount(operacion.getCantidad());
        operationRecordDTO.setStatus(true); // 0 = operación activa

        OperationRecordEntity operationRecordEntitySave = operationRecordRepository.save(modelMapper.map(operationRecordDTO, OperationRecordEntity.class));
        if (operationRecordEntitySave != null) {
            return modelMapper.map(operationRecordEntitySave, OperationRecordDTO.class);
        } else {
            throw new RuntimeException("operationRecord NO guardada. Error");
        }
    }


    //este metodo CAMBIO. fijate donde se utiliza
    public SellOperationNumberDTO doesSellOperationNumberServiceExist(Operacion operacion) {

        SellOperationNumberDTO sellOperationNumberDTO = new SellOperationNumberDTO();
        sellOperationNumberDTO.setNumber(operacion.getNumero().longValue());
        sellOperationNumberDTO.setAmount(operacion.getCantidad());
        sellOperationNumberDTO.setSimbol(operacion.getSimbolo());
        if (sellOperationNumberService.exist(operacion.getNumero().longValue(), false)) {
            sellOperationNumberDTO.setStatus(false);//donde se llame hay q verificar. Si es false, este sellOperation esta cerrado!
            return sellOperationNumberDTO;

        } else if (sellOperationNumberService.exist(operacion.getNumero().longValue(), true)) {
            sellOperationNumberDTO.setStatus(true);//vendio una parte y existe ya en nuestra base. se puede procesar el resto y actualizar
            return sellOperationNumberDTO;
        }
        else {
            sellOperationNumberDTO.setStatus(null);//esta operacion nunca se registro. Se procesa y se guarda. si vende t0do-> status=false
            return sellOperationNumberDTO;
        }
    }

    public boolean isPurchaseGreaterThanOrEqualToTheSale(SellOperationNumberDTO sellOperationNumberDTO, OperationRecordDTO operationRecordDTO, Long purchaseAmount, Long sellAmount) {


        if (purchaseAmount >= sellAmount) {

            //actualizamos valor residual y el estado de la venta
            sellOperationNumberDTO.setResidualQuantity(0L);
            sellOperationNumberDTO.setStatus(false);
            if (sellOperationNumberService.save(sellOperationNumberDTO)) {
                //si t0do va bien
                long res = purchaseAmount - sellAmount;
                if (res == 0) {
                    operationRecordDTO.setSalesAmount(operationRecordDTO.getPurchaseAmount());
                    operationRecordDTO.setStatus(false); //si la cantidad que cierro son iguales, operationRecord queda completa y cerrada
                } else {
                    operationRecordDTO.setSalesAmount(operationRecordDTO.getSalesAmount() + sellAmount);
                }
                OperationRecordEntity operationRecordSaved = operationRecordRepository.save(modelMapper.map(operationRecordDTO, OperationRecordEntity.class));
                if (operationRecordSaved == null) {
                    throw new RuntimeException();
                }
                return true;
            }
            throw new RuntimeException();
        } else {
            return false;
        }
    }

    public boolean isThePurchaseLessThanTheSale(Long sellAmount, Long purchaseAmount, SellOperationNumberDTO sellOperationNumberDTO, OperationRecordDTO operationRecordDTO) {
        //sellAmount y purchaseStoCancel como parametros.. hacer bloque del resto
        Long result = sellAmount - purchaseAmount;
        Long sold = sellAmount - result;

        //-----------------------------------------------------------------------------------
        if (sellOperationNumberDTO.getResidualQuantity() != null) {
            sellOperationNumberDTO.setResidualQuantity(sellOperationNumberDTO.getResidualQuantity() + sold);
        } else {
            sellOperationNumberDTO.setResidualQuantity(sold);
        }

        //guardamos la operacion!
        if (sellOperationNumberService.save(sellOperationNumberDTO)) {
            //si se actualizo correctamente el residual en sellOperation, guardamos operationRecord

            //en este caso, la operacion quedaria completa y cerrada
            operationRecordDTO.setSalesAmount(operationRecordDTO.getPurchaseAmount());
            operationRecordDTO.setStatus(false);
            OperationRecordEntity operationRecordEntity = operationRecordRepository.save(modelMapper.map(operationRecordDTO, OperationRecordEntity.class));
            if (operationRecordEntity == null) {
                throw new RuntimeException();
            }
            return true;
        } else {
            throw new RuntimeException();
        }
        //-----------------------------------------------------------------------------------
    }

    //este metodo se encargara de hacer los calculos de rendimientos, rendimientosAnualizados y resultado(positiva/negativa)
    @Override
    @Transactional
    public void closedOperationRecordProcessor(){
        Optional<List<OperationRecordEntity>> operationRecordOptional = operationRecordRepository.findNullResult();
        if (operationRecordOptional.get() == null){
            throw new RuntimeException();
        }
        //List de solo lectura!
        OperationRecordDTO[] operationRecordDTOS = modelMapper.map(operationRecordOptional.get(),OperationRecordDTO[].class);

        for (OperationRecordDTO operation: operationRecordDTOS) {
            //Por cada operacion en estado cerrada que no ha sido procesada acá comienza su camino. Primero:
            //Rendimiento:

            Double purchasePrice = operation.getPurchasePrice();
            Double salePrice = operation.getSalePrice();

            Double yield = ((salePrice*100)/purchasePrice)-100;

            operation.setYield(yield);

            //rendimiento anualizado:
            // Ranualizado = (1+Rtotal)^(365/n) - 1
            //donde n periodo de dias en la operacion

            Duration duracion = Duration.between(operation.getDateOfPurchase(), operation.getSaleDate());

            String duracionString = String.valueOf(duracion.toDays());

            Double rate = 365.0 / Double.parseDouble(duracionString);
            Double annualizedYield = Math.pow(1 + yield, rate) - 1;

            operation.setAnnualizedYield(annualizedYield);

            //resultado
            if(operation.getPurchasePrice()>operation.getSalePrice()){
                operation.setResult("positiva");
            }
            else {
                operation.setResult("negativa");
            }
            OperationRecordEntity operationRecordEntityUpdated = operationRecordRepository.save(modelMapper.map(operation,OperationRecordEntity.class));
            if(operationRecordEntityUpdated == null){
                throw new RuntimeException("No se actualizo el rendimiento/rend.anualizado/resultado");
            }
        }

    }
}



package developBot.MervalOperations.services.mervalBotDataService.impl;

import developBot.MervalOperations.entities.OperationRecordEntities.OperationRecordEntity;
import developBot.MervalOperations.models.clientModel.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.OperationRecord.BuyOperationNumber;
import developBot.MervalOperations.models.OperationRecord.OperationRecord;
import developBot.MervalOperations.models.OperationRecord.SellOperationNumber;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
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

        //tene en cuenta si es cancelada pueda haber operado una parte de la posicion de compra
        //si demando 10, me ofertaron 5, se opero y luego se cancelo la orden
        if (operacion.getTipo().equals("Compra") && (operacion.getEstado().equals("terminada") ||
                (operacion.getEstado().equals("cancelada") && operacion.getCantidadOperada() != null))) {

            BuyOperationNumber buyOperationNumber = doesBuyNumberOperationServiceExist(operacion);
            if (buyOperationNumber == null) {
                throw new RuntimeException("este numero de operacion se encuentra registrado");
            }

            OperationRecord operationRecord = saveBuyOperationNumberDTO(buyOperationNumber, operacion);
            if (operationRecord == null) {
                throw new RuntimeException("Error operationalRecord en la compra");
            }


        } else if (operacion.getTipo().equals("Venta") && (operacion.getEstado().equals("terminada") || (operacion.getEstado().equals("cancelada") && operacion.getCantidadOperada() != null))) {
            //si es venta tenemos que verificar que no haya sido cargada
            //si no esta la cargamos
            //preguntamos si existe esta orden y que el status sea cerrado.Si es abierto todavia hay operaciones disponibles

            SellOperationNumber sellOperationNumber = doesSellOperationNumberServiceExist(operacion);
            if (!sellOperationNumber.getStatus()) {
                throw new RuntimeException();//operacion cerrada
            }
            //hasta aca verificamos que el sellOperation no existe o que existe en estado abierto. En otro caso devolvera una exepcion


            //busca operationsRecord en estado abiertas y las procesa
            Optional<OperationRecordEntity> operationRecordEntityOpt = operationRecordRepository.findBySimbolAndStatus(sellOperationNumber.getSimbol(), true);
            if (operationRecordEntityOpt.isEmpty()) {
                //no hay ningun activo (operacion de compra) abierto con este simbolo
                throw new RuntimeException();
            }

            OperationRecord operationRecord = modelMapper.map(operationRecordEntityOpt.get(), OperationRecord.class);



            //este debe encargarse de varias cosas:
            //_luego de concretar la venta (si es que previamente-en este codigo- se encontro un operationalRecord con este simbolo y con compras a cancelar)
            //-debe concretar 2 procesos importantes. Primero actualizar el sellOperation. con cuidado en el residualQuantity y el status
            //si etsa ok, debe actualizar el operationRecord!
            sellEjecution(operationRecord, sellOperationNumber);
        }
    }


    //
    //
    //
    //
    //este metodo debe actualizarse
    @Transactional
    public void sellEjecution(OperationRecord operationRecord, SellOperationNumber sellOperationNumber){
        Long sellAmount;
        //cantidad vendida
        Long quantitysold = operationRecord.getSalesAmount();

        Long purchaseAmount = operationRecord.getPurchaseAmount();

        //si nunca realizo una venta en este operationalRecord(que contiene una compra) estara en null o 0-> Se usa el total del purchase
        if (quantitysold == null || quantitysold == 0) {

            //primero las cantidades disponibles de compra y las de venta.
            if (sellOperationNumber.getResidualQuantity() == null) {
                sellAmount = sellOperationNumber.getAmount();
            } else {
                sellAmount = sellOperationNumber.getResidualQuantity();
                if (sellAmount == 0) {
                    throw new RuntimeException();//ene este caso no habria nada para vender.es decir, la venta de esta operacion se termino
                }
            }

            //este método actualiza el status del sellOperation, la cantidad resiudal es 0L y se cierra, es decir, se vende
            boolean fin = isPurchaseGreaterThanOrEqualToTheSale(sellOperationNumber, operationRecord, purchaseAmount, sellAmount);


            if (!fin) {
                if (!isThePurchaseLessThanTheSale(sellAmount, purchaseAmount, sellOperationNumber, operationRecord)) {
                    throw new RuntimeException("la operacion de venta via isPurchaseGreaterThanOrEqualToTheSale no funciono");
                }
            }


        } else if (quantitysold > 0) {
            //si ya se realizo ventas en el operationRecord hay 2 opciones
            //o es mas alto o es mas bajo que mi cantidad de venta

            //
            long purchaseStoCancel = operationRecord.getPurchaseAmount() - operationRecord.getSalesAmount();

            if (sellOperationNumber.getResidualQuantity() == null) {
                sellAmount = sellOperationNumber.getAmount();//nunca se introdujo un valor, implica que nunca se vendio nada.
            } else {
                sellAmount = sellOperationNumber.getResidualQuantity();
                if (sellAmount == 0) {
                    throw new RuntimeException();
                    //recorda que sellAmount es basicamente el residual del sellOperationNumber, si es 0, esta terminada
                }
            }


            //este método,si es ture; actualiza el status del sellOperation, la cantidad resiudal es 0L y se cierra, es decir, se vende
            boolean fin = isPurchaseGreaterThanOrEqualToTheSale(sellOperationNumber, operationRecord, purchaseStoCancel, sellAmount);


            if (!fin) {
                if (!isThePurchaseLessThanTheSale(sellAmount, purchaseStoCancel, sellOperationNumber, operationRecord)) {
                    throw new RuntimeException("la operacion de venta via isPurchaseGreaterThanOrEqualToTheSale no funciono");
                }
            }

        }
    }

    public BuyOperationNumber doesBuyNumberOperationServiceExist(Operacion operacion) {
        if (!buyOperationNumberService.exists(operacion.getNumero().longValue())) {
            //si no existe guardamos la operacion de compra

            BuyOperationNumber buyOperationNumber = new BuyOperationNumber();
            buyOperationNumber.setNumber(operacion.getNumero().longValue());
            buyOperationNumber.setSimbol(operacion.getSimbolo());
            buyOperationNumber.setAmount(operacion.getCantidadOperada().longValue());

            return buyOperationNumber;
        } else {
            throw new RuntimeException("este numbero de operacion ya existe");
        }
    }

    public OperationRecord saveBuyOperationNumberDTO(BuyOperationNumber buyOperationNumber, Operacion operacion) {
        //si se guardo la operacion de compra, generamos en operationRecord(contendrá la compra/venta de activos)

        OperationRecord operationRecord = new OperationRecord();
        operationRecord.setSimbol(operacion.getSimbolo());
        operationRecord.setDateOfPurchase(operacion.getFechaOperada());
        operationRecord.setPurchasePrice(operacion.getPrecioOperado());
        operationRecord.setPurchaseAmount(operacion.getCantidad());
        operationRecord.setStatus(true); // 0 = operación activa

        OperationRecordEntity operationRecordEntitySave = operationRecordRepository.save(modelMapper.map(operationRecord, OperationRecordEntity.class));
        if (operationRecordEntitySave != null) {
            return modelMapper.map(operationRecordEntitySave, OperationRecord.class);
        } else {
            throw new RuntimeException("operationRecord NO guardada. Error");
        }
    }


    //este metodo CAMBIO. fijate donde se utiliza
    public SellOperationNumber doesSellOperationNumberServiceExist(Operacion operacion) {

        SellOperationNumber sellOperationNumber = new SellOperationNumber();
        sellOperationNumber.setNumber(operacion.getNumero().longValue());
        sellOperationNumber.setAmount(operacion.getCantidad());
        sellOperationNumber.setSimbol(operacion.getSimbolo());
        if (sellOperationNumberService.exist(operacion.getNumero().longValue(), false)) {
            sellOperationNumber.setStatus(false);//donde se llame hay q verificar. Si es false, este sellOperation esta cerrado!
            return sellOperationNumber;

        } else if (sellOperationNumberService.exist(operacion.getNumero().longValue(), true)) {
            sellOperationNumber.setStatus(true);//vendio una parte y existe ya en nuestra base. se puede procesar el resto y actualizar
            return sellOperationNumber;
        }
        else {
            sellOperationNumber.setStatus(null);//esta operacion nunca se registro. Se procesa y se guarda. si vende t0do-> status=false
            return sellOperationNumber;
        }
    }

    public boolean isPurchaseGreaterThanOrEqualToTheSale(SellOperationNumber sellOperationNumber, OperationRecord operationRecord, Long purchaseAmount, Long sellAmount) {


        if (purchaseAmount >= sellAmount) {

            //actualizamos valor residual y el estado de la venta, queda liquidada
            sellOperationNumber.setResidualQuantity(0L);
            sellOperationNumber.setStatus(false);
            if (sellOperationNumberService.save(sellOperationNumber)) {

                //si t0do va bien
                long res = purchaseAmount - sellAmount;
                if (res == 0) {
                    operationRecord.setSalesAmount(operationRecord.getPurchaseAmount());
                    List<SellOperationNumber> listSell = operationRecord.getSellOperationNumbers();
                    listSell.add(sellOperationNumber);
                    operationRecord.setSellOperationNumbers(listSell);
                    operationRecord.setSaleDate(LocalDateTime.now());
                    //hacer metodo que calcule precio promedio de venta y la cantidad vendida(puede devender de 1 o mas SellOperationNumber)

                    operationRecord.setStatus(false); //si la cantidad que cierro son iguales, operationRecord queda completa y cerrada
                } else {
                    operationRecord.setSalesAmount(operationRecord.getSalesAmount() + sellAmount);
                }
                OperationRecordEntity operationRecordSaved = operationRecordRepository.save(modelMapper.map(operationRecord, OperationRecordEntity.class));
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


    //este metodo da por hecho de que el purcheaseAmount es menor a sellAmount
    public boolean isThePurchaseLessThanTheSale(Long sellAmount, Long purchaseAmount, SellOperationNumber sellOperationNumber, OperationRecord operationRecord) {
        //sellAmount y purchaseStoCancel como parametros.. hacer bloque del resto
        Long result = sellAmount - purchaseAmount;
        Long sold = sellAmount - result;

        //-----------------------------------------------------------------------------------
        sellOperationNumber.setStatus(true);

        if (sellOperationNumber.getResidualQuantity() != null) {
            sellOperationNumber.setResidualQuantity(sellOperationNumber.getResidualQuantity() + sold);

        } else {
            sellOperationNumber.setResidualQuantity(sold);
        }

        //guardamos la operacion!
        if (sellOperationNumberService.save(sellOperationNumber)) {
            //si se actualizo correctamente el residual en sellOperation, guardamos operationRecord

            //en este caso, la operacion quedaria completa y cerrada
            operationRecord.setSalesAmount(operationRecord.getPurchaseAmount());
            operationRecord.setStatus(false);
            OperationRecordEntity operationRecordEntity = operationRecordRepository.save(modelMapper.map(operationRecord, OperationRecordEntity.class));
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
        OperationRecord[] operationRecords = modelMapper.map(operationRecordOptional.get(), OperationRecord[].class);

        for (OperationRecord operation: operationRecords) {
            //Por cada operacion en estado cerrada que no ha sido procesada acá comienza su camino. Primero:
            //Rendimiento:

            Double purchasePrice = operation.getPurchasePrice();
            Double salePrice = operation.getAverageSellingPrice();

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
            if(operation.getPurchasePrice()>operation.getAverageSellingPrice()){
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



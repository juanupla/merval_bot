package developBot.MervalOperations.services.botMervalService.impl;

import developBot.MervalOperations.models.clientModel.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModel.miCuenta.portafolio.Posicion;
import developBot.MervalOperations.models.dto.ClientJwtUtilDTO;
import developBot.MervalOperations.models.dto.OperationRecordDto.OperationRecordDTO;
import developBot.MervalOperations.services.botMervalService.BotMervalBusienssService;
import developBot.MervalOperations.services.botMervalService.BotMervalService;
import developBot.MervalOperations.services.iolApiService.ClientJwtUtilService;
import developBot.MervalOperations.services.mervalBotDataService.OperationRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BotMervalServiceImpl implements BotMervalService {
    private List<String> activosBot;

    @Autowired
    private BotMervalBusienssService botMervalBusienssService;
    //private OperationRecordService operationRecordService = new OperationRecordServiceImpl();
    @Autowired
    private ClientJwtUtilService clientJwtUtilService;
    @Autowired
    private OperationRecordService operationRecordService;
    private Boolean state;
    private Timer timer;
    @Override
    public void ejecution() {

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    LocalDateTime now = LocalDateTime.now();
                    DayOfWeek dia = now.getDayOfWeek();
                    String nombre = dia.name();
                    if (!nombre.equals("SUNDAY") && !nombre.equals("SATURDAY") &&
                            LocalDateTime.now().isAfter(LocalDateTime.now().withHour(11).withMinute(0).withSecond(0))
                            && LocalDateTime.now().isBefore(LocalDateTime.now().withHour(16).withMinute(58).withSecond(0))) {

                        int intentos = 3;
                        while (intentos>0){
                            try {
                                state = true;

                                ClientJwtUtilDTO clientJwtUtilDTO = clientJwtUtilService.getToken();

                                //si al token le falta menos de 4 minutos para expirar refresca el token
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
                                ZonedDateTime zonedDateTime = ZonedDateTime.parse(clientJwtUtilDTO.getExpires(), formatter);

                                //convertir a zona horaria local
                                String zonaHoraria = "America/Buenos_Aires"; // Reemplaza con tu zona horaria
                                ZonedDateTime localZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of(zonaHoraria));
                                LocalDateTime localDateTime = localZonedDateTime.toLocalDateTime();
                                if (LocalDateTime.now().plusMinutes(4).isAfter(localDateTime)) {
                                    clientJwtUtilDTO = clientJwtUtilService.getToken();
                                }


                                //Activos operados por el bot:
                                String totalTickets = "AMZN,GOOGL,TSLA,GLOB,AMD,VIST,CEPU,EDN,TGNO4,TGSU2,BYMA,NVDA,YPFD,MSFT,PAMP,QCOM,COME,DISN,MELI,AAPL,BA.C,MCD,GOLD,PG,META,PBR,NKE,WMT,V,NFLX,CAT,BMA,GGAL,SBUX,ARKK,JPM";
                                //
                                String[] elementos = totalTickets.split(",");
                                activosBot = Arrays.asList(elementos);//Recordá, esta lista es de SOLO LECTURA, por su consturcion


                                StringBuilder print = new StringBuilder("Lista de activos considerados: ");

                                activosBot = botMervalBusienssService.removeOperationalTickets(clientJwtUtilDTO.getAccesToken(), "argentina", activosBot); //devuelve un nuevo ArrayList, se puede manipular

                                if (activosBot != null) {
                                    boolean flag = false;
                                    for (String ticket : activosBot) {
                                        if (!flag) {
                                            print.append(" ").append(ticket);
                                            flag = true;
                                        } else {
                                            print.append(", ").append(ticket);
                                        }
                                    }
                                }
                                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
                                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
                                System.out.println(print);


                                List<Operacion> deletePendingOperations = botMervalBusienssService.removePendingOrders(clientJwtUtilDTO.getAccesToken());
                                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
                                System.out.println("La Lista de operaciones pendientes eliminadas es de: " + deletePendingOperations.size() + " operaciones");


                                //este bloque toma los activos del portafolio y resvisa su posible venta
                                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
                                System.out.println("Observacion de venta de activos en cartera: ");
                                List<Posicion> ticketsEnCartera = botMervalBusienssService.operationalTickets(clientJwtUtilDTO.getAccesToken(), "argentina");
                                boolean flag = false;
                                if (ticketsEnCartera != null) {
                                    for (Posicion p : ticketsEnCartera) {
                                        List<BigDecimal> emas = botMervalBusienssService.calculoEMAs(clientJwtUtilDTO.getAccesToken(), p.getTitulo().getSimbolo().toUpperCase());
                                        if (botMervalBusienssService.EMAsSaleOperation(emas)) {
                                            //System.out.println("activo entrando en venta: "+p.getTitulo().getSimbolo());
                                            boolean venta = botMervalBusienssService.saleOperation(clientJwtUtilDTO.getAccesToken(), emas, p);
                                            if (venta) {
                                                System.out.println("-------------------------------------------------------");
                                                flag = true;
                                            }
                                        }
                                    }
                                }
                                if (!flag) {
                                    System.out.println("No hay ventas sobre ningun activo en cartera");
                                }
                                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");

                                System.out.println("Observacion de compra de activos: ");

                                //bloque que recorre los activos actuales de la lista, sin los activos presentes en nuestra cartera y revisa posible compra
                                if (activosBot != null) {
                                    for (int i = activosBot.size(); i > 0; ) {

                                        int numeroAzar = (int) (Math.random() * activosBot.size());

                                        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
                                        System.out.println("-------------------------------------------------------");
                                        System.out.println("Los resultado de las EMAs (3,9,21,50) del tiket " + activosBot.get(numeroAzar) + " son: ");


                                        List<BigDecimal> list = botMervalBusienssService.calculoEMAs(clientJwtUtilDTO.getAccesToken(), activosBot.get(numeroAzar).toUpperCase());
                                        for (BigDecimal big : list) {
                                            System.out.println(big.setScale(10, RoundingMode.HALF_UP));
                                        }

                                        boolean puedeComprar = botMervalBusienssService.EMAsPurchaseOperation(clientJwtUtilDTO.getAccesToken(), activosBot.get(numeroAzar).toUpperCase(), list);
                                        System.out.println("-------------------------------------------------------");
                                        if (puedeComprar) {
                                            botMervalBusienssService.purchaseOperation(clientJwtUtilDTO.getAccesToken(), activosBot.get(numeroAzar).toUpperCase(), list);
                                            activosBot.remove(numeroAzar);
                                            i = activosBot.size();
                                        } else {
                                            System.out.println("el ticket " + activosBot.get(numeroAzar).toUpperCase() + " está fuera de rango estrategico y NO fue operado");
                                            activosBot.remove(numeroAzar);
                                            i = activosBot.size();
                                        }

                                    }
                                }

                                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
                                System.out.println("Sistema duerme 15 minutos en la fecha: " + LocalDateTime.now().toString());


                            }
                            catch (Error e){
                                intentos--;
                                state=false;
                            }

                        }
                        if (LocalDateTime.now().isAfter(LocalDateTime.now().withHour(16).withMinute(35).withSecond(0)) &&
                                LocalDateTime.now().isBefore(LocalDateTime.now().withHour(19).withMinute(10).withSecond(0))) {

                            //metodos para actualizar la base de datos respecto de oepraciones
                            ClientJwtUtilDTO clientJwtUtilDTO = clientJwtUtilService.getToken();

                            if (operationRecordService.updateOperationsDataBase(clientJwtUtilDTO.getAccesToken())) {
                                System.out.println("andubo el servicio");
                            }
                        }
                        //crear un boolean: si estamos fuera de horario de mercado, qué horario es y dormirlo hasta la apertura siguiente
                    }
                    if (!nombre.equals("SUNDAY") && !nombre.equals("SATURDAY") && !botMervalBusienssService.isItFeriado(LocalDateTime.now())) {
                        if (LocalDateTime.now().isAfter(LocalDateTime.now().withHour(16).withMinute(35).withSecond(0)) &&
                                LocalDateTime.now().isBefore(LocalDateTime.now().withHour(19).withMinute(10).withSecond(0))) {


                            //puede ir estructura try/catch
                            //metodos para actualizar la base de datos respecto de oepraciones
                            ClientJwtUtilDTO clientJwtUtilDTO = clientJwtUtilService.getToken();

                            if (operationRecordService.updateOperationsDataBase(clientJwtUtilDTO.getAccesToken())) {
                                System.out.println("andubo el servicio");
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        }, 0, 15 * 60 * 1000); // ejecuta cada 15 minutos

        do {

//            if (LocalDateTime.now().isAfter(LocalDateTime.now().withHour(16).withMinute(35).withSecond(0)) &&
//                    LocalDateTime.now().isBefore(LocalDateTime.now().withHour(19).withMinute(10).withSecond(0))) {
//                //metodos para actualizar la base de datos
//                OperationRecordDTO operationRecordDTO = new OperationRecordDTO();
//                if (operationRecordService.updateOperationsDataBase(operationRecordDTO)) {
//                    System.out.println("andubo el servicio");
//                }
//
//            }

        } while (true);
    }
}

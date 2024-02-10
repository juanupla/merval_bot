package developBot.MervalOperations.busienss;

import developBot.MervalOperations.authentication.JwtUtil;
import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;

import developBot.MervalOperations.models.clientModels.miCuenta.portafolio.Posicion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BotMerval {

    private List<String> activosBot;

    private BotMervalBusiness botMervalService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ModelMapper modelMapper = new ModelMapper();


    public void ejecution() throws InterruptedException {


        JwtUtil prueba = new JwtUtil();
        String token = prueba.getToken();
        CallsApiIOL callsApiIOL= new CallsApiIOL();
        botMervalService = new BotMervalBusiness(callsApiIOL);


        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dia = now.getDayOfWeek();
        String nombre = dia.name();

        do {

            //si al token le falta menos de 4 minutos para expirar refresca el token
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(prueba.getExpires(), formatter);

            //convertir a zona horaria local
            String zonaHoraria = "America/Buenos_Aires"; // Reemplaza con tu zona horaria
            ZonedDateTime localZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of(zonaHoraria));
            LocalDateTime localDateTime = localZonedDateTime.toLocalDateTime();
            if(LocalDateTime.now().plusMinutes(4).isAfter(localDateTime)){
                token = prueba.getToken();
            }


            //Activos operados por el bot:
            String totalTickets = "AMZN,GOOGL,TSLA,GLOB,AMD,VIST,CEPU,EDN,TGNO4,TGSU2,BYMA,NVDA,YPFD,MSFT,PAMP,QCOM,COME,DISN,MELI,AAPL,BA.C,MCD,GOLD,PG,META,PBR,NKE,WMT,V,NFLX,CAT,BMA,GGAL,SBUX,ARKK,JPM";
            //
            String[] elementos = totalTickets.split(",");
            activosBot = Arrays.asList(elementos);//Recordá, esta lista es de SOLO LECTURA, por su consturcion


            StringBuilder print = new StringBuilder("Lista de activos considerados: ");

            activosBot = botMervalService.removeOperationalTickets(token,"argentina",activosBot); //devuelve un nuevo ArrayList, se puede manipular

            if(activosBot != null){
                boolean flag = false;
                for (String ticket:activosBot) {
                    if (!flag){
                        print.append(" ").append(ticket);
                        flag = true;
                    }
                    else {
                        print.append(", ").append(ticket);
                    }
                }
            }
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println(print);


            List<Operacion> deletePendingOperations = botMervalService.removePendingOrders(token);
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("La Lista de operaciones pendientes eliminadas es de: " + deletePendingOperations.size() + " operaciones");


            //este bloque toma los activos del portafolio y resvisa su posible venta
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("Observacion de venta de activos en cartera: ");
            List<Posicion> ticketsEnCartera = botMervalService.operationalTickets(token,"argentina");
            boolean flag = false;
            if (ticketsEnCartera != null){
                for (Posicion p : ticketsEnCartera) {
                    List<BigDecimal> emas = botMervalService.calculoEMAs(token,p.getTitulo().getSimbolo().toUpperCase());
                    if (botMervalService.EMAsSaleOperation(emas)){
                        //System.out.println("activo entrando en venta: "+p.getTitulo().getSimbolo());
                        boolean venta = botMervalService.saleOperation(token,emas,p);
                        if(venta){
                            System.out.println("-------------------------------------------------------");
                            flag = true;
                        }
                    }
                }
            }
            if(!flag) {
                System.out.println("No hay ventas sobre ningun activo en cartera");
            }
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");

            System.out.println("Observacion de compra de activos: ");

            //bloque que recorre los activos actuales de la lista, sin los activos presentes en nuestra cartera y revisa posible compra
            if(activosBot != null){
                for (int i = activosBot.size(); i > 0;) {

                    int numeroAzar = (int) (Math.random() * activosBot.size());

                    System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
                    System.out.println("-------------------------------------------------------");
                    System.out.println("Los resultado de las EMAs (3,9,21,50) del tiket "+activosBot.get(numeroAzar)+" son: ");




                    List<BigDecimal> list = botMervalService.calculoEMAs(token,activosBot.get(numeroAzar).toUpperCase());
                    for (BigDecimal big: list) {
                        System.out.println(big.setScale(10, RoundingMode.HALF_UP));
                    }

                    boolean puedeComprar = botMervalService.EMAsPurchaseOperation(token,activosBot.get(numeroAzar).toUpperCase(),list);
                    System.out.println("-------------------------------------------------------");
                    if(puedeComprar){
                        botMervalService.purchaseOperation(token,activosBot.get(numeroAzar).toUpperCase(),list);
                        activosBot.remove(numeroAzar);
                        i = activosBot.size();
                    }
                    else {
                        System.out.println("el ticket "+ activosBot.get(numeroAzar).toUpperCase() + " está fuera de rango estrategico y NO fue operado");
                        activosBot.remove(numeroAzar);
                        i = activosBot.size();
                    }

                }
            }

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("Sistema duerme 15 minutos en la fecha: " + LocalDateTime.now().toString());

            Thread.sleep(900000);//duerme 15 minutos

        }while (!nombre.equals("SUNDAY") && !nombre.equals("SATURDAY") &&
                LocalDateTime.now().isAfter(LocalDateTime.now().withHour(11).withMinute(0).withSecond(0))
                && LocalDateTime.now().isBefore(LocalDateTime.now().withHour(16).withMinute(58).withSecond(0)));

    }
}

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
            String totalTickets = "AMZN,GOOGL,TSLA,GLOB,AMD,VIST,CEPU,EDN,TGNO4,TGSU2,LOMA,BYMA,NVDA,YPFD,MSFT,PAMP,HAVA,AGRO,COME,PYPL,DISN,MELI,AAPL,BA.C,MCD,GOLD,PG,META,PBR,NKE,WMT,V,NFLX,VALE,CAT,BABA,BMA,SUPV,GGAL,ARKK";

            String[] elementos = totalTickets.split(",");
            activosBot = Arrays.asList(elementos);


            StringBuilder print = new StringBuilder("Lista de activos considerados: ");

            activosBot = botMervalService.removeOperationalTickets(token,"argentina",activosBot);

            if(activosBot != null){
                boolean flag = false;
                for (String ticket:activosBot) {
                    if (!flag){
                        print.append(" ").append(ticket);
                        flag = true;
                    }
                    print.append(", ").append(ticket);
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
            if (ticketsEnCartera != null){
                for (Posicion p : ticketsEnCartera) {
                    List<BigDecimal> emas = botMervalService.calculoEMAs(token,p.getTitulo().getSimbolo().toUpperCase());
                    if (botMervalService.EMAsSaleOperation(emas)){
                        boolean venta = botMervalService.saleOperation(token,emas,p);
                        if(venta){
                            System.out.println("-------------------------------------------------------");
                        }
                    }
                }
            }
            else {
                System.out.println("No hay ventas sobre ningun activo en cartera");
            }
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");



            //bloque que recorre los activos actuales de la lista, sin los activos presentes en nuestra cartera y revisa posible compra
            if(activosBot != null){
                for (int i = 0; i < activosBot.size();i++) {
                    System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
                    System.out.println("-------------------------------------------------------");
                    System.out.println("Los resultado de las EMAs (3,9,21,50) del tiket "+activosBot.get(i)+" son: ");
                    List<BigDecimal> list = botMervalService.calculoEMAs(token,activosBot.get(i).toUpperCase());
                    for (BigDecimal big: list) {
                        System.out.println(big.setScale(10, RoundingMode.HALF_UP));
                    }
                    boolean puedeComprar = botMervalService.EMAsPurchaseOperation(token,activosBot.get(i).toUpperCase(),list);
                    System.out.println("-------------------------------------------------------");
                    if(puedeComprar){
                        botMervalService.purchaseOperation(token,activosBot.get(i).toUpperCase(),list);
                    }

                }
            }

            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("Sistema duerme 30 minutos en la fecha: " + LocalDateTime.now().toString());

            Thread.sleep(1800000);//duerme 30 minutos



        }while (!nombre.equals("SUNDAY") && !nombre.equals("SATURDAY") &&
                now.isAfter(LocalDateTime.now().withHour(11).withMinute(0).withSecond(0))
                && now.isBefore(LocalDateTime.now().withHour(17).withMinute(0).withSecond(0)));

    }
}

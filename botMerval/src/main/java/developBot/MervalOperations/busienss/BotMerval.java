package developBot.MervalOperations.busienss;

import developBot.MervalOperations.authentication.JwtUtil;
import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.service.BotMervalService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private BotMervalServiceImpl botMervalService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ModelMapper modelMapper = new ModelMapper();


    public void ejecution() throws InterruptedException {


        JwtUtil prueba = new JwtUtil();
        String token = prueba.getToken();
        CallsApiIOL callsApiIOL= new CallsApiIOL();
        botMervalService = new BotMervalServiceImpl(callsApiIOL);


        do {

            //si al token le falta menos de 6 minutos para expirar refresca el token
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
            String totalTickets = "AMZN,GOOGL,CEPU,EDN,PAMP,PYPL,HAVA,AGRO,LOMA,BYMA,COME,ARKK,TRAN";//,VIST,TSLA,DISN,NVDA,MELI,AAPL,AMD,YPFD,MSFT,BA.C,MCD,GOLD," +
            //"PG,META,PBR,NKE,WMT,V,NFLX,BABA,VALE,CAT,GLOB"

            String[] elementos = totalTickets.split(",");
            activosBot = Arrays.asList(elementos);


            StringBuilder print = new StringBuilder("Lista de activos considerados: ");


            //a este metodo con botMervalService habria que agregarle la funcionalidad de que revise los activos en cartera y si las condiciones
            // de ventas estan dadas que las ejecute!
            activosBot = botMervalService.removeOperationalTickets(token,"argentina",activosBot);

            boolean flag = false;
            for (String ticket:activosBot) {
                if (!flag){
                    print.append(" ").append(ticket);
                    flag = true;
                }
                print.append(", ").append(ticket);
            }
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println(print);


            List<Operacion> deletePendingOperations = botMervalService.removePendingOrders(token);
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("La Lista de operaciones pendientes es de: " + deletePendingOperations.size() + " operaciones");




            //aca puede ir el proceso de ventas de activos
            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");


            System.out.println("-------------------------------------------------------");



            //bloque que recorre los activos actuales de la lista
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
                    System.out.println("El activo: " + activosBot.get(i).toUpperCase()+" puede ser operado para compra");
                }
                else {
                    System.out.println("El activo: " + activosBot.get(i).toUpperCase()+" NO puede ser operado para compra");
                }



                //esto de abajo hay que borrarlo solo es para probar el metodo .EMAsSaleOperation que deberia funcionar
                boolean puedeVender = botMervalService.EMAsSaleOperation(list);
                if(puedeVender){
                    System.out.println("El activo: " + activosBot.get(i).toUpperCase()+" puede ser operado para venta");
                }
                else {
                    System.out.println("El activo: " + activosBot.get(i).toUpperCase()+" NO puede ser operado para venta");
                }
            }







            System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("Sistema duerme 4 minutos en la fecha: " + LocalDateTime.now().toString());

            Thread.sleep(240000);//duerme 4 minutos

        }while (true);//acá la condicion debe pasar a una estructura horaria que verifique si el horario esta entre las 11 y las 17hs

    }
}

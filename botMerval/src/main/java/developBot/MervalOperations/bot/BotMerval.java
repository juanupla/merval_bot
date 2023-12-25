package developBot.MervalOperations.bot;

import developBot.MervalOperations.authentication.JwtUtil;
import developBot.MervalOperations.models.clientModels.miCuenta.operaciones.Operacion;
import developBot.MervalOperations.models.clientModels.titulos.cotizacion.Cotizacion;
import developBot.MervalOperations.service.BotMervalService;
import developBot.MervalOperations.service.impl.BotMervalServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BotMerval {

    private List<String> activosBot;

    private BotMervalService botMervalService;

    public void ejecution() {
        //Activos operados por el bot:
        String totalTickets = "AMZN,GOGL,MSFT,VIST,TSLA,DISN,NVDA,MELI,AAPL,AMD,YPF,CEPU,EDN,BA.C,PAMP,MCD,GOLD," +
                "PG,META,PBR,NKE,WMT,PYPL,V,NFLX,HAVA,AGRO,LOMA,BYMA,COME,BABA,VALE,CAT,GLOB,ARKK";
        String[] elementos = totalTickets.split(",");
        activosBot = Arrays.asList(elementos);

        JwtUtil prueba = new JwtUtil();
        botMervalService = new BotMervalServiceImpl();

        List<String> fin = botMervalService.removeOperationalTickets(prueba.getToken(),"argentina",activosBot);
        for (String ticket:fin) {
            System.out.println(ticket);
        }

        List<Operacion> deletePendingOperations = botMervalService.removePendingOrders(prueba.getToken());
        System.out.println("la Lista de operaciones pendientes es de: " + deletePendingOperations.size() + " operaciones");

        boolean cot = botMervalService.calculoEMAs(prueba.getToken(),"meli");
        if(cot){
            System.out.println("ema positiva : Compra");
        }
    }
}
